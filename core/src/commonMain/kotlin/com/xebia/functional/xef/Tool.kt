package com.xebia.functional.xef

import com.xebia.functional.openai.generated.model.FunctionObject
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.llm.FunctionCall
import com.xebia.functional.xef.llm.StreamedFunction
import com.xebia.functional.xef.llm.chatFunction
import kotlin.jvm.JvmName
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1
import kotlin.reflect.KSuspendFunction1
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.json.*

sealed class Tool<out A>(
  open val function: FunctionObject,
  open val invoke: suspend (FunctionCall) -> A
) {

  data class Enumeration<out E>(
    override val function: FunctionObject,
    override val invoke: suspend (FunctionCall) -> E,
    val cases: List<Tool<E>>,
    val enumSerializer: (String) -> E
  ) : Tool<E>(function = function, invoke = invoke)

  class FlowOfStrings :
    Tool<Nothing>(function = FunctionObject("", ""), invoke = { error("Not invoked") })

  class FlowOfStreamedFunctions<out A>(
    override val function: FunctionObject,
    override val invoke: suspend (FunctionCall) -> A
  ) : Tool<A>(function = function, invoke = invoke)

  class FlowOfAIEvents<out A>(
    val serializer: Tool<A>,
    override val function: FunctionObject = serializer.function,
    override val invoke: suspend (FunctionCall) -> A = { serializer.invoke(it) }
  ) : Tool<A>(function = function, invoke = invoke)

  class FlowOfAIEventsSealed<A>(
    val sealedSerializer: Sealed<A>,
    override val function: FunctionObject = sealedSerializer.function,
    override val invoke: suspend (FunctionCall) -> A = { sealedSerializer.invoke(it) }
  ) : Tool<A>(function = function, invoke = invoke)

  data class Sealed<A>(
    override val function: FunctionObject,
    override val invoke: suspend (FunctionCall) -> A,
    val cases: List<Case>,
  ) : Tool<A>(function = function, invoke = invoke) {
    data class Case(val className: String, val tool: Tool<*>)
  }

  data class Contextual<A>(
    override val function: FunctionObject,
    override val invoke: suspend (FunctionCall) -> A,
  ) : Tool<A>(function = function, invoke = invoke)

  data class Callable<A>(
    override val function: FunctionObject,
    override val invoke: suspend (FunctionCall) -> A,
  ) : Tool<A>(function = function, invoke = invoke)

  data class Primitive<A>(
    override val function: FunctionObject,
    override val invoke: suspend (FunctionCall) -> A
  ) : Tool<A>(function = function, invoke = invoke)

  companion object {

    inline fun <reified A> fromKotlin(): Tool<A> = fromKotlin(typeOf<A>())

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    fun <A : Any> fromKotlin(type: KType): Tool<A> {
      val targetClass = type.getTargetClass<A>()
      val descriptor = targetClass.serializer().descriptor
      val kind = descriptor.kind
      return when {
        kind == PolymorphicKind.SEALED -> sealedTool(targetClass, descriptor)
        kind == SerialKind.ENUM -> enumerationTool(targetClass, descriptor)
        type == typeOf<Flow<String>>() -> flowOfStringsTool()
        isFlowOfStreamedFunctions(targetClass, type) -> flowOfStreamedFunctionsTool(type)
        isFlowOfAiEventsSealed(targetClass, type) -> flowOfAIEventsSealedTool(type)
        isFlowOfAIEvents(targetClass, type) -> flowOfAIEventsTool(type)
        requiresWrapping(type) -> wrappedValueTool(type, targetClass)
        else -> defaultClassTool(targetClass)
      }
    }

    @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
    private fun <A : Any> isFlowOfAiEventsSealed(
      targetClass: KClass<A>,
      type: KType,
    ): Boolean {
      val innerTargetClass =
        type.arguments.firstOrNull()?.type?.arguments?.firstOrNull()?.type?.classifier as? KClass<*>
      val kind = innerTargetClass?.serializer()?.descriptor?.kind
      return isFlowOfAIEvents(targetClass, type) && kind == PolymorphicKind.SEALED
    }

    private fun isFlowOfAIEvents(targetClass: KClass<*>, type: KType): Boolean =
      targetClass == Flow::class && type.arguments[0].type?.classifier == AIEvent::class

    private fun isFlowOfStreamedFunctions(targetClass: KClass<*>, type: KType): Boolean =
      targetClass == Flow::class && type.arguments[0].type?.classifier == StreamedFunction::class

    private fun <A : Any> wrappedValueTool(type: KType, targetClass: KClass<A>): Tool<A> {
      val collectionTypeArg = type.arguments.firstOrNull()?.type?.classifier as? KClass<*>
      return if (collectionTypeArg != null) {
        collectionTool(collectionTypeArg, targetClass)
      } else {
        primitiveTool(targetClass)
      }
    }

    private fun <A : Any> KType.getTargetClass(): KClass<A> =
      (classifier as? KClass<*> ?: error("Expected KClass got $classifier")) as KClass<A>

    @OptIn(InternalSerializationApi::class)
    private fun <A : Any> defaultClassTool(targetClass: KClass<A>): Tool<A> {
      val typeSerializer = targetClass.serializer()
      val functionObject = chatFunction(typeSerializer.descriptor)
      return Callable(functionObject) {
        Config.DEFAULT.json.decodeFromString(typeSerializer, it.arguments)
      }
    }

    @OptIn(InternalSerializationApi::class)
    private fun <A : Any> primitiveTool(targetClass: KClass<A>): Tool<A> {
      val functionSerializer = Input.serializer(targetClass.serializer())
      val functionObject = chatFunction(functionSerializer.descriptor)
      return Primitive(functionObject) {
        Config.DEFAULT.json.decodeFromString(functionSerializer, it.arguments).argument
      }
    }

    @OptIn(InternalSerializationApi::class)
    private fun <A> collectionTool(
      collectionTypeArg: KClass<*>,
      targetClass: KClass<*>
    ): Callable<A> {
      val innerSerializer = collectionTypeArg.serializer()
      val functionSerializer =
        when (targetClass) {
          List::class -> {
            Input.serializer(ListSerializer(innerSerializer))
          }
          Set::class -> {
            Input.serializer(SetSerializer(innerSerializer))
          }
          else -> {
            error("Unsupported collection type: $targetClass, expected List or Set")
          }
        }
      val functionObject = chatFunction(functionSerializer.descriptor)
      return Callable(functionObject) {
        Config.DEFAULT.json.decodeFromString(functionSerializer, it.arguments).argument as A
      }
    }

    @OptIn(InternalSerializationApi::class)
    private fun <A : Any> flowOfAIEventsSealedTool(type: KType): FlowOfAIEventsSealed<A> {
      val targetType = flowInnerContainerTypeArg(type)
      val targetClass = (targetType?.classifier as? KClass<*>)
      val typeSerializer = targetClass?.serializer() ?: error("No serializer found for $targetType")
      val tool = sealedTool<A>(targetClass, typeSerializer.descriptor)
      return FlowOfAIEventsSealed(tool)
    }

    private fun <A : Any> flowOfAIEventsTool(type: KType): FlowOfAIEvents<A> {
      val targetType = flowInnerContainerTypeArg(type)
      val functionSerializer =
        targetType?.let { fromKotlin<A>(it) } ?: error("No serializer found for $type")
      return FlowOfAIEvents(functionSerializer)
    }

    @OptIn(InternalSerializationApi::class)
    private fun <A : Any> flowOfStreamedFunctionsTool(
      type: KType,
    ): FlowOfStreamedFunctions<A> {
      val targetType = flowInnerContainerTypeArg(type)
      val typeSerializer =
        (targetType?.classifier as? KClass<*>)?.serializer()
          ?: error("No serializer found for $targetType")
      val functionSerializer = fromKotlin<A>(targetType)
      val functionObject = chatFunction(typeSerializer.descriptor)
      return FlowOfStreamedFunctions(functionObject) { functionSerializer.invoke(it) }
    }

    private fun flowInnerContainerTypeArg(type: KType): KType? =
      type.arguments[0].type?.arguments?.get(0)?.type

    private fun flowOfStringsTool(): FlowOfStrings = FlowOfStrings()

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    private fun <A> enumerationTool(
      targetClass: KClass<*>,
      descriptor: SerialDescriptor
    ): Enumeration<A> {
      val enumSerializer = { value: String ->
        Config.DEFAULT.json.decodeFromString(targetClass.serializer(), value) as A
      }
      val functionObject = chatFunction(descriptor)
      val cases =
        descriptor.elementDescriptors.map {
          val enumValue = it.serialName
          val enumFunction = chatFunction(it)
          Primitive(enumFunction) { enumSerializer(enumValue) }
        }
      return Enumeration(functionObject, { error("should not get called") }, cases, enumSerializer)
    }

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    private fun <A> sealedTool(targetClass: KClass<*>, descriptor: SerialDescriptor): Sealed<A> {
      val sealedClassSerializer =
        targetClass.serializer() as? SealedClassSerializer
          ?: error("expected SealedClassSerializer got ${targetClass.serializer()}")
      val casesDescriptors =
        sealedClassSerializer.descriptor.elementDescriptors.toList()[1].elementDescriptors.toList()
      val functionObjectMap = casesDescriptors.associateWith { chatFunction(it) }
      val cases =
        casesDescriptors.map {
          val caseFunction =
            functionObjectMap[it] ?: error("No function found for ${it.serialName}")
          Sealed.Case(
            tool =
              Callable(caseFunction) {
                callSealedCase<A>(it, functionObjectMap, sealedClassSerializer)
              },
            className = it.serialName
          )
        }
      return Sealed(
        chatFunction(descriptor),
        { callSealedCase(it, functionObjectMap, sealedClassSerializer) },
        cases
      )
    }

    @OptIn(InternalSerializationApi::class)
    private fun <A> callSealedCase(
      it: FunctionCall,
      functionObjectMap: Map<SerialDescriptor, FunctionObject>,
      sealedClassSerializer: SealedClassSerializer<out Any>
    ): A {
      val newJson = descriptorChoice(it, functionObjectMap)
      return Config.DEFAULT.json.decodeFromString(
        sealedClassSerializer,
        Json.encodeToString(newJson)
      ) as A
    }

    private fun descriptorChoice(
      call: FunctionCall,
      descriptors: Map<SerialDescriptor, FunctionObject>
    ): JsonObject {
      // adds a `type` field with the call.functionName serial name equivalent to the call arguments
      val jsonWithDiscriminator =
        Config.DEFAULT.json.decodeFromString(JsonElement.serializer(), call.arguments)
      val descriptor =
        descriptors.values.firstOrNull { it.name.endsWith(call.functionName) }
          ?: error("No descriptor found for ${call.functionName}")
      val targetDescriptor =
        descriptors.keys.firstOrNull { it.serialName.endsWith(call.functionName) }
          ?: error("No descriptor found for ${call.functionName}")
      val newJson =
        JsonObject(
          jsonWithDiscriminator.jsonObject +
            (Config.TYPE_DISCRIMINATOR to JsonPrimitive(targetDescriptor.serialName))
        )
      return newJson
    }

    private fun requiresWrapping(type: KType): Boolean {
      val targetClass =
        type.classifier as? KClass<*> ?: error("expected KClass got ${type.classifier}")
      return when (targetClass) {
        List::class,
        Int::class,
        String::class,
        Boolean::class,
        Double::class,
        Float::class,
        Char::class,
        Byte::class -> true
        else -> false
      }
    }

    @JvmName("fromKotlinFunction1")
    inline operator fun <reified A, reified B, reified F : (A) -> B> invoke(
      name: String,
      description: Description,
      fn: F,
    ): Tool<B> {
      val tool = fromKotlin<A>()
      return Callable(
        function = tool.function.copy(name = name, description = description.value),
        invoke = {
          val input = tool.invoke(it)
          fn(input)
        }
      )
    }

    @JvmName("fromKotlinSuspendFunction1")
    inline fun <reified A, reified B> suspend(
      name: String,
      description: Description,
      noinline fn: suspend (A) -> B,
    ): Tool<B> {
      val tool = fromKotlin<A>()
      return Callable(
        function = tool.function.copy(name = name, description = description.value),
        invoke = {
          val input = tool.invoke(it)
          fn(input)
        }
      )
    }

    @JvmName("fromKotlinKFunction1")
    inline operator fun <reified A, reified B, reified F : KFunction1<A, B>> invoke(
      fn: F,
      description: Description = Description(fn.name)
    ): Tool<B> {
      val tool = fromKotlin<A>()
      return Callable(
        function = tool.function.copy(name = fn.name, description = description.value),
        invoke = {
          val input = tool.invoke(it)
          fn(input)
        }
      )
    }

    @JvmName("fromKotlinKSuspendFunction1")
    inline operator fun <reified A, B> invoke(
      fn: KSuspendFunction1<A, B>,
      description: Description = Description(fn.name)
    ): Tool<B> {
      val tool = fromKotlin<A>()
      return Callable(
        function = tool.function.copy(name = fn.name, description = description.value),
        invoke = {
          val input = tool.invoke(it)
          fn(input)
        }
      )
    }
  }
}
