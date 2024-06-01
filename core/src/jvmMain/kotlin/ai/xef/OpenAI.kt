package ai.xef

import ai.xef.langchain4j.*
import com.knuddels.jtokkit.Encodings
import com.xebia.functional.xef.ClassifierAI
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName
import dev.langchain4j.model.openai.OpenAiEmbeddingModel
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import dev.langchain4j.model.openai.OpenAiTokenizer
import kotlinx.serialization.serializer
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.typeOf

object OpenAI {

  class Embeddings @JvmOverloads constructor(
    embeddings: OpenAiEmbeddingModel,
    override val modelName: String = embeddings.modelName()
  ) : Langchain4JEmbeddings(embeddings, embeddings.modelName()) {
    companion object {

      @JvmStatic
      fun text_embeddings_3_small(
        config: Config = Config()
      ): Embeddings =
        openAIEmbeddingsModel(OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL.name, config)

      private fun openAIEmbeddingsModel(
        modelName: String,
        config: Config
      ): Embeddings =
        Embeddings(
          OpenAiEmbeddingModel.builder()
            .modelName(modelName)
            .tokenizer(OpenAiTokenizer())
            .apiKey(config.token)
            .organizationId(config.org)
            .baseUrl(config.baseUrl)
            .logRequests(config.logRequests)
            .logResponses(config.logResponses)
            .build()
        )
    }
  }

  class Chat @JvmOverloads constructor(
    chat: OpenAiChatModel,
    streaming: OpenAiStreamingChatModel,
    override val tokenizer: TokenizerImpl = TokenizerImpl(OpenAiTokenizer(), encoding(chat))
  ) : Langchain4JChat(
    chat = chat,
    streamingChat = streaming,
    modelName = chat.modelName(),
    tokenPaddingSum = 0,
    tokenPadding = 0,
    maxContextLength = 0
  ) {

    @AiDsl
    suspend inline operator fun <reified A: Any> invoke(
      prompt: String,
      conversation: Conversation = Conversation()
    ): A =
      invoke(
        prompt = Prompt(prompt),
        conversation = conversation
      )

    suspend inline fun <reified E> classify(
      input: String,
      context: String,
      value: String
    ): E where E: Enum<E>, E: ClassifierAI.PromptClassifier =
      Langchain4JAIClassifier<E>(
        target = typeOf<E>(),
        model = this,
        serializer = { serializer() },
        enumSerializer = {
          enumValueOf(it)
        }
      ).invoke(

      )

    suspend inline operator fun <reified A: Any> invoke(
      prompt: Prompt,
      conversation: Conversation = Conversation()
    ): A =
      Langchain4JAI<A>(
        target = typeOf<A>(),
        model = this,
        serializer = { serializer() },
        enumSerializer = null
      ).invoke(prompt, conversation)

    companion object {
      private fun encoding(chat: OpenAiChatModel) =
        Encodings.newLazyEncodingRegistry().getEncodingForModel(chat.modelName()).getOrNull()
          ?: error("No encoding found for model ${chat.modelName()}")

      @JvmStatic
      fun gpt3_5_turbo(
        config: Config = Config()
      ): Chat =
        openAIChatAndStreamingModel(OpenAiChatModelName.GPT_3_5_TURBO.name, config)

      @JvmStatic
      fun gpt4o(
        config: Config = Config()
      ): Chat =
        openAIChatAndStreamingModel(OpenAiChatModelName.GPT_4_O.name, config)

      private fun openAIChatAndStreamingModel(
        modelName: String,
        config: Config
      ): Chat =
        Chat(
          chat = openAiChatModel(modelName, config),
          streaming = openAiStreamingChatModel(modelName, config)
        )

      private fun openAiStreamingChatModel(modelName: String, config: Config): OpenAiStreamingChatModel =
        OpenAiStreamingChatModel.builder()
          .modelName(modelName)
          .tokenizer(OpenAiTokenizer())
          .apiKey(config.token)
          .organizationId(config.org)
          .baseUrl(config.baseUrl)
          .logRequests(config.logRequests)
          .logResponses(config.logResponses)
          .build()

      private fun openAiChatModel(modelName: String, config: Config): OpenAiChatModel =
        OpenAiChatModel.builder()
          .modelName(modelName)
          .tokenizer(OpenAiTokenizer())
          .apiKey(config.token)
          .organizationId(config.org)
          .baseUrl(config.baseUrl)
          .logRequests(config.logRequests)
          .logResponses(config.logResponses)
          .build()
    }
  }
}
