package ai.xef.langchain4j

import ai.xef.stream.AIEvent
import ai.xef.langchain4j.streams.KotlinAiServiceTokenStream
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.ToolExecutionResultMessage
import dev.langchain4j.exception.IllegalConfigurationException
import dev.langchain4j.internal.Exceptions
import dev.langchain4j.model.input.PromptTemplate
import dev.langchain4j.model.input.structured.StructuredPrompt
import dev.langchain4j.model.input.structured.StructuredPromptProcessor
import dev.langchain4j.model.moderation.Moderation
import dev.langchain4j.model.output.Response
import dev.langchain4j.model.output.TokenUsage
import dev.langchain4j.rag.AugmentationRequest
import dev.langchain4j.rag.AugmentationResult
import dev.langchain4j.rag.query.Metadata
import dev.langchain4j.service.*
import kotlinx.coroutines.flow.*
import java.io.InputStream
import java.lang.reflect.*
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KType

class KotlinAIServices<T : Any>(
  private val typeOfT: KType,
  private val target: Class<*> = (typeOfT.classifier as? KClass<*>)?.java
    ?: error("Expected type ${typeOfT.classifier} to be a class"),
  context: AiServiceContext = AiServiceContext(target)
) : AiServices<T>(context) {


  override fun build(): T {
    performBasicValidation()

    context.aiServiceClass.methods.forEach { method ->
      validateMethodModeration(method)
      if (method.returnType == Result::class.java) {
        validateResultReturnType(method)
      }
    }

    val proxyInstance = Proxy.newProxyInstance(
      context.aiServiceClass.classLoader,
      arrayOf(context.aiServiceClass),
      createInvocationHandler()
    )

    return proxyInstance as T
  }

  private fun createInvocationHandler(): InvocationHandler {
    val executor: ExecutorService = Executors.newCachedThreadPool()
    return object : InvocationHandler {
      @Throws(Exception::class)
      override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any {
        if (method.declaringClass == Any::class.java) {
          return method.invoke(this, *args)
        }

        validateParameters(method)

        val memoryId = findMemoryId(method, args)?.toString() ?: DEFAULT
        val systemMessage = prepareSystemMessage(memoryId, method, args)
        var userMessage = prepareUserMessage(method, args)

        val augmentationResult = augmentIfNeeded(userMessage, memoryId)
        if (augmentationResult != null) {
          userMessage = augmentationResult.chatMessage() as dev.langchain4j.data.message.UserMessage
        }

        val returnType = resolveReturnType(method)
        val userMessageWithFormat = appendOutputFormatInstructions(userMessage, returnType)

        if (context.hasChatMemory()) {
          val chatMemory = context.chatMemory(memoryId)
          systemMessage?.let { chatMemory.add(it) }
          chatMemory.add(userMessageWithFormat)
        }

        val messages = collectMessages(systemMessage, userMessageWithFormat, memoryId)
        val moderationFuture = triggerModerationIfNeeded(method, messages)

        return when (returnType) {
          TokenStream::class.java -> AiServiceTokenStream(messages, context, memoryId)
          Flow::class.java -> handleFlowReturnType(method, messages, memoryId)
          else -> handleDefaultReturnType(method, memoryId, messages.toMutableList(), moderationFuture, augmentationResult)
        }
      }

      private fun augmentIfNeeded(userMessage: dev.langchain4j.data.message.UserMessage, memoryId: String): AugmentationResult? {
        return context.retrievalAugmentor?.let {
          val chatMemory = context.chatMemory(memoryId)?.messages()
          val metadata = Metadata.from(userMessage, memoryId, chatMemory)
          val augmentationRequest = AugmentationRequest(userMessage, metadata)
          it.augment(augmentationRequest)
        }
      }

      private fun resolveReturnType(method: Method): Class<*> {
        var returnType = method.returnType
        if (returnType == Result::class.java) {
          val annotatedReturnType = method.annotatedReturnType
          val type = annotatedReturnType.type as ParameterizedType
          returnType = Class.forName(type.actualTypeArguments.first().typeName)
        }
        return returnType
      }

      private fun appendOutputFormatInstructions(userMessage: dev.langchain4j.data.message.UserMessage, returnType: Class<*>): dev.langchain4j.data.message.UserMessage {
        val outputFormatInstructions = ServiceOutputParser.outputFormatInstructions(returnType)
        return dev.langchain4j.data.message.UserMessage.from(userMessage.text() + outputFormatInstructions)
      }

      private fun collectMessages(systemMessage: dev.langchain4j.data.message.SystemMessage?, userMessage: dev.langchain4j.data.message.UserMessage, memoryId: String): List<ChatMessage> {
        return if (context.hasChatMemory()) {
          context.chatMemory(memoryId).messages()
        } else {
          listOfNotNull(systemMessage, userMessage)
        }
      }

      private fun handleFlowReturnType(method: Method, messages: List<ChatMessage>, memoryId: String): Flow<*> {
        val returnType = resolveFlowReturnType(method)
        val producedFlow = KotlinAiServiceTokenStream<Any>(returnType, messages, context, memoryId).flow()
        return if (returnType == String::class.java) {
          producedFlow.filterIsInstance<AIEvent.Chunk>().map { it.chunk }
        } else {
          producedFlow
        }
      }

      private fun resolveFlowReturnType(method: Method): Class<*> {
        val annotatedReturnType = method.annotatedReturnType
        val type = annotatedReturnType.type as ParameterizedType
        val typeArguments = type.actualTypeArguments

        return when {
          typeArguments.first().typeName in listOf("java.lang.String", "kotlin.String") -> String::class.java
          else -> {
            val targetTypeName = (typeArguments.first() as? ParameterizedType)?.actualTypeArguments?.firstOrNull()?.typeName
            Class.forName(targetTypeName)
          }
        }
      }

      private fun handleDefaultReturnType(
        method: Method,
        memoryId: String,
        messages: MutableList<ChatMessage>,
        moderationFuture: Future<Moderation>?,
        augmentationResult: AugmentationResult?
      ): Any {
        var response = generateResponse(messages)
        var tokenUsageAccumulator = response.tokenUsage()

        verifyModerationIfNeeded(moderationFuture)

        var executionsLeft = MAX_SEQUENTIAL_TOOL_EXECUTIONS
        while (true) {
          if (executionsLeft-- == 0) {
            throw Exceptions.runtime(
              "Something is wrong, exceeded %s sequential tool executions",
              MAX_SEQUENTIAL_TOOL_EXECUTIONS
            )
          }

          val aiMessage = response.content()
          updateChatMemory(memoryId, messages, aiMessage)

          if (!aiMessage.hasToolExecutionRequests()) {
            break
          }

          response = handleToolExecutionRequests(aiMessage, messages, memoryId)
          tokenUsageAccumulator = TokenUsage.sum(tokenUsageAccumulator, response.tokenUsage())
        }

        response = Response.from(response.content(), tokenUsageAccumulator, response.finishReason())
        val parsedResponse = ServiceOutputParser.parse(response, method.returnType)

        return if (method.returnType == Result::class.java) {
          Result.builder<Any>()
            .content(parsedResponse)
            .tokenUsage(tokenUsageAccumulator)
            .sources(augmentationResult?.contents())
            .build()
        } else {
          parsedResponse
        }
      }

      private fun generateResponse(messages: List<ChatMessage>): Response<AiMessage> {
        return if (context.toolSpecifications == null) {
          context.chatModel.generate(messages)
        } else {
          context.chatModel.generate(messages, context.toolSpecifications)
        }
      }

      private fun updateChatMemory(memoryId: String, messages: MutableList<ChatMessage>, aiMessage: ChatMessage) {
        if (context.hasChatMemory()) {
          context.chatMemory(memoryId).add(aiMessage)
        } else {
          messages += aiMessage
        }
      }

      private fun handleToolExecutionRequests(
        aiMessage: AiMessage,
        messages: MutableList<ChatMessage>,
        memoryId: String,
      ): Response<AiMessage> {
        aiMessage.toolExecutionRequests().forEach { toolExecutionRequest ->
          val toolExecutor = context.toolExecutors[toolExecutionRequest.name()]
          val toolExecutionResult = toolExecutor!!.execute(toolExecutionRequest, memoryId)
          val toolExecutionResultMessage = ToolExecutionResultMessage.from(toolExecutionRequest, toolExecutionResult)
          updateChatMemory(memoryId, messages, toolExecutionResultMessage)
        }

        return generateResponse(messages)
      }

      private fun triggerModerationIfNeeded(method: Method, messages: List<ChatMessage?>): Future<Moderation>? {
        return if (method.isAnnotationPresent(Moderate::class.java)) {
          executor.submit<Moderation> {
            val messagesToModerate = removeToolMessages(messages)
            context.moderationModel.moderate(messagesToModerate).content()
          }
        } else {
          null
        }
      }
    }
  }


  private fun validateMethodModeration(method: Method) {
    if (method.isAnnotationPresent(Moderate::class.java) && context.moderationModel == null) {
      throw IllegalConfigurationException.illegalConfiguration(
        "The @Moderate annotation is present, but the moderationModel is not set up. " +
          "Please ensure a valid moderationModel is configured before using the @Moderate annotation."
      )
    }
  }

  private fun prepareSystemMessage(
    memoryId: Any,
    method: Method,
    args: Array<Any>
  ): dev.langchain4j.data.message.SystemMessage? {
    return findSystemMessageTemplate(memoryId, method)
      ?.let { systemMessageTemplate: String ->
        PromptTemplate.from(systemMessageTemplate)
          .apply(findTemplateVariables(systemMessageTemplate, method, args))
          .toSystemMessage()
      }
  }

  private fun findSystemMessageTemplate(memoryId: Any, method: Method): String? {
    val annotation = method.getAnnotation(
      SystemMessage::class.java
    )
    if (annotation != null) {
      return getTemplate(
        method,
        "System",
        annotation.fromResource,
        annotation.value,
        annotation.delimiter
      )
    }

    return context.systemMessageProvider.apply(memoryId).getOrNull()
  }

  companion object {
    private const val MAX_SEQUENTIAL_TOOL_EXECUTIONS = 10

    fun validateParameters(method: Method) {
      val parameters = method.parameters
      if (parameters == null || parameters.size < 2) {
        return
      }

      for (parameter in parameters) {
        val v = parameter.getAnnotation(V::class.java)
        val userMessage = parameter.getAnnotation(
          UserMessage::class.java
        )
        val memoryId = parameter.getAnnotation(MemoryId::class.java)
        val userName = parameter.getAnnotation(UserName::class.java)
        if (v == null && userMessage == null && memoryId == null && userName == null) {
          throw IllegalConfigurationException.illegalConfiguration(
            "Parameter '%s' of method '%s' should be annotated with @V or @UserMessage " +
              "or @UserName or @MemoryId", parameter.name, method.name
          )
        }
      }
    }

    private fun findTemplateVariables(template: String?, method: Method, args: Array<Any>): Map<String, Any> {
      val parameters = method.parameters

      val variables: MutableMap<String, Any> = HashMap()
      for (i in parameters.indices) {
        val annotation = parameters[i].getAnnotation(V::class.java)
        if (annotation != null) {
          val variableName = annotation.value
          val variableValue = args[i]
          variables[variableName] = variableValue
        }
      }

      if (template!!.contains("{{it}}") && !variables.containsKey("it")) {
        val itValue = getValueOfVariableIt(parameters, args)
        variables["it"] = itValue
      }

      return variables
    }

    private fun getValueOfVariableIt(parameters: Array<Parameter>, args: Array<Any>): String {
      if (parameters.size == 1) {
        val parameter = parameters[0]
        if (!parameter.isAnnotationPresent(MemoryId::class.java)
          && !parameter.isAnnotationPresent(UserMessage::class.java)
          && !parameter.isAnnotationPresent(UserName::class.java)
          && (!parameter.isAnnotationPresent(V::class.java) || isAnnotatedWithIt(parameter))
        ) {
          return toString(args[0])
        }
      }

      for (i in parameters.indices) {
        if (isAnnotatedWithIt(parameters[i])) {
          return toString(args[i])
        }
      }

      throw IllegalConfigurationException.illegalConfiguration("Error: cannot find the value of the prompt template variable \"{{it}}\".")
    }

    private fun isAnnotatedWithIt(parameter: Parameter): Boolean {
      val annotation = parameter.getAnnotation(V::class.java)
      return annotation != null && "it" == annotation.value
    }

    private fun prepareUserMessage(method: Method, args: Array<Any>): dev.langchain4j.data.message.UserMessage {
      val template = getUserMessageTemplate(method, args)
      val variables = findTemplateVariables(template, method, args)

      val prompt = PromptTemplate.from(template).apply(variables)

      val maybeUserName = findUserName(method.parameters, args)
      return maybeUserName?.let { userName: String? ->
        dev.langchain4j.data.message.UserMessage.from(
          userName,
          prompt.text()
        )
      } ?: prompt.toUserMessage()
    }

    private fun getUserMessageTemplate(method: Method, args: Array<Any>): String {
      val templateFromMethodAnnotation = findUserMessageTemplateFromMethodAnnotation(method)
      val templateFromParameterAnnotation = findUserMessageTemplateFromAnnotatedParameter(method.parameters, args)

      if (templateFromMethodAnnotation != null && templateFromParameterAnnotation != null) {
        throw IllegalConfigurationException.illegalConfiguration(
          "Error: The method '%s' has multiple @UserMessage annotations. Please use only one.",
          method.name
        )
      }

      if (templateFromMethodAnnotation != null) {
        return templateFromMethodAnnotation
      }
      if (templateFromParameterAnnotation != null) {
        return templateFromParameterAnnotation
      }

      val templateFromTheOnlyArgument = findUserMessageTemplateFromTheOnlyArgument(method.parameters, args)
      if (templateFromTheOnlyArgument != null) {
        return templateFromTheOnlyArgument
      }

      throw IllegalConfigurationException.illegalConfiguration(
        "Error: The method '%s' does not have a user message defined.",
        method.name
      )
    }

    private fun findUserMessageTemplateFromMethodAnnotation(method: Method): String? {
      return method.getAnnotation(
        UserMessage::class.java
      )?.let { a: UserMessage -> getTemplate(method, "User", a.fromResource, a.value, a.delimiter) }
    }

    private fun findUserMessageTemplateFromAnnotatedParameter(
      parameters: Array<Parameter>,
      args: Array<Any>
    ): String? {
      for (i in parameters.indices) {
        if (parameters[i].isAnnotationPresent(UserMessage::class.java)) {
          return toString(args[i])
        }
      }
      return null
    }

    private fun findUserMessageTemplateFromTheOnlyArgument(
      parameters: Array<Parameter>?,
      args: Array<Any>
    ): String? {
      if (parameters != null && parameters.size == 1 && parameters[0].annotations.isEmpty()) {
        return toString(args[0])
      }
      return null
    }

    private fun findUserName(parameters: Array<Parameter>, args: Array<Any>): String? {
      for (i in parameters.indices) {
        if (parameters[i].isAnnotationPresent(UserName::class.java)) {
          return args[i].toString()
        }
      }
      return null
    }

    private fun getTemplate(
      method: Method,
      type: String,
      resource: String,
      value: Array<String>,
      delimiter: String
    ): String {
      val messageTemplate: String?
      if (!resource.trim { it <= ' ' }.isEmpty()) {
        messageTemplate = getResourceText(method.declaringClass, resource)
        if (messageTemplate == null) {
          throw IllegalConfigurationException.illegalConfiguration(
            "@%sMessage's resource '%s' not found",
            type,
            resource
          )
        }
      } else {
        messageTemplate = java.lang.String.join(delimiter, *value)
      }
      if (messageTemplate!!.trim { it <= ' ' }.isEmpty()) {
        throw IllegalConfigurationException.illegalConfiguration("@%sMessage's template cannot be empty", type)
      }
      return messageTemplate
    }

    private fun getResourceText(clazz: Class<*>, name: String): String? {
      return getText(clazz.getResourceAsStream(name))
    }

    private fun getText(inputStream: InputStream?): String? {
      if (inputStream == null) {
        return null
      }
      Scanner(inputStream).use { scanner ->
        scanner.useDelimiter("\\A").use { s ->
          return if (s.hasNext()) s.next() else ""
        }
      }
    }

    private fun findMemoryId(method: Method, args: Array<Any>): Any? {
      val parameters = method.parameters
      for (i in parameters.indices) {
        if (parameters[i].isAnnotationPresent(MemoryId::class.java)) {
          val memoryId = args.getOrNull(i)
            ?: throw Exceptions.illegalArgument(
              "The value of parameter '%s' annotated with @MemoryId in method '%s' must not be null",
              parameters[i].name, method.name
            )
          return memoryId
        }
      }
      return null
    }

    private fun toString(arg: Any): String {
      return if (arg.javaClass.isArray) {
        arrayToString(arg)
      } else if (arg.javaClass.isAnnotationPresent(StructuredPrompt::class.java)) {
        StructuredPromptProcessor.toPrompt(arg).text()
      } else {
        arg.toString()
      }
    }

    private fun arrayToString(arg: Any): String {
      val sb = StringBuilder("[")
      val length = java.lang.reflect.Array.getLength(arg)
      for (i in 0 until length) {
        sb.append(toString(java.lang.reflect.Array.get(arg, i)))
        if (i < length - 1) {
          sb.append(", ")
        }
      }
      sb.append("]")
      return sb.toString()
    }
  }
}
