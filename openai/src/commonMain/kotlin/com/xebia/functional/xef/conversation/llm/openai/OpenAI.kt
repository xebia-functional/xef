package com.xebia.functional.xef.conversation.llm.openai

import arrow.core.nonEmptyListOf
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.AutoClose
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.PlatformConversation
import com.xebia.functional.xef.conversation.autoClose
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore
import com.xebia.functional.xef.tracing.Dispatcher
import com.xebia.functional.xef.tracing.createDispatcher
import kotlin.jvm.JvmField
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

private const val KEY_ENV_VAR = "OPENAI_TOKEN"
private const val HOST_ENV_VAR = "OPENAI_HOST"

class OpenAI(internal var token: String? = null, internal var host: String? = null,   internal val dispatcher: Dispatcher = createDispatcher()) :
  AutoCloseable, AutoClose by autoClose() {

  private fun openAITokenFromEnv(): String {
    return getenv(KEY_ENV_VAR)
      ?: throw AIError.Env.OpenAI(nonEmptyListOf("missing $KEY_ENV_VAR env var"))
  }

  private fun openAIHostFromEnv(): String? {
    return getenv(HOST_ENV_VAR)
  }

  fun getToken(): String {
    return token ?: openAITokenFromEnv()
  }

  fun getHost(): String? {
    return host
      ?: run {
        host = openAIHostFromEnv()
        host
      }
  }

  init {
    if (token == null) {
      token = openAITokenFromEnv()
    }
    if (host == null) {
      host = openAIHostFromEnv()
    }
  }

  val GPT_4 by lazy { autoClose(OpenAIModel(this, "gpt-4", ModelType.GPT_4, dispatcher)) }

  val GPT_4_0314 by lazy { autoClose(OpenAIModel(this, "gpt-4-0314", ModelType.GPT_4, dispatcher)) }

  val GPT_4_32K by lazy { autoClose(OpenAIModel(this, "gpt-4-32k", ModelType.GPT_4_32K, dispatcher)) }

  val GPT_3_5_TURBO by lazy {
    autoClose(OpenAIModel(this, "gpt-3.5-turbo", ModelType.GPT_3_5_TURBO, dispatcher))
  }

  val GPT_3_5_TURBO_16K by lazy {
    autoClose(OpenAIModel(this, "gpt-3.5-turbo-16k", ModelType.GPT_3_5_TURBO_16_K, dispatcher))
  }

  val GPT_3_5_TURBO_FUNCTIONS by lazy {
    autoClose(OpenAIModel(this, "gpt-3.5-turbo-0613", ModelType.GPT_3_5_TURBO_FUNCTIONS, dispatcher))
  }

  val GPT_3_5_TURBO_0301 by lazy {
    autoClose(OpenAIModel(this, "gpt-3.5-turbo-0301", ModelType.GPT_3_5_TURBO, dispatcher))
  }

  val TEXT_DAVINCI_003 by lazy {
    autoClose(OpenAIModel(this, "text-davinci-003", ModelType.TEXT_DAVINCI_003, dispatcher))
  }

  val TEXT_DAVINCI_002 by lazy {
    autoClose(OpenAIModel(this, "text-davinci-002", ModelType.TEXT_DAVINCI_002, dispatcher))
  }

  val TEXT_CURIE_001 by lazy {
    autoClose(OpenAIModel(this, "text-curie-001", ModelType.TEXT_SIMILARITY_CURIE_001, dispatcher))
  }

  val TEXT_BABBAGE_001 by lazy {
    autoClose(OpenAIModel(this, "text-babbage-001", ModelType.TEXT_BABBAGE_001, dispatcher))
  }

  val TEXT_ADA_001 by lazy { autoClose(OpenAIModel(this, "text-ada-001", ModelType.TEXT_ADA_001, dispatcher)) }

  val TEXT_EMBEDDING_ADA_002 by lazy {
    autoClose(OpenAIModel(this, "text-embedding-ada-002", ModelType.TEXT_EMBEDDING_ADA_002, dispatcher))
  }

  val DALLE_2 by lazy { autoClose(OpenAIModel(this, "dalle-2", ModelType.GPT_3_5_TURBO, dispatcher)) }

  @JvmField
  val DEFAULT_CHAT = GPT_3_5_TURBO_16K

  @JvmField
  val DEFAULT_SERIALIZATION = GPT_3_5_TURBO_FUNCTIONS

  @JvmField
  val DEFAULT_EMBEDDING = TEXT_EMBEDDING_ADA_002

  @JvmField
  val DEFAULT_IMAGES = DALLE_2

  fun supportedModels(): List<OpenAIModel> {
    return listOf(
      GPT_4,
      GPT_4_0314,
      GPT_4_32K,
      GPT_3_5_TURBO,
      GPT_3_5_TURBO_16K,
      GPT_3_5_TURBO_FUNCTIONS,
      GPT_3_5_TURBO_0301,
      TEXT_DAVINCI_003,
      TEXT_DAVINCI_002,
      TEXT_CURIE_001,
      TEXT_BABBAGE_001,
      TEXT_ADA_001,
      TEXT_EMBEDDING_ADA_002,
      DALLE_2
    )
  }

  companion object {
    @JvmField
    val FromEnvironment: OpenAI = OpenAI(null, null, createDispatcher())

    @JvmSynthetic
    suspend inline fun <A> conversation(
      store: VectorStore,
      dispatcher: Dispatcher = createDispatcher(),
      noinline block: suspend Conversation.() -> A
    ): A = block(conversation(dispatcher, store))

    @JvmSynthetic
    suspend fun <A> conversation(dispatcher: Dispatcher = createDispatcher(), block: suspend Conversation.() -> A): A =
      block(conversation(dispatcher,LocalVectorStore(FromEnvironment.DEFAULT_EMBEDDING)))

    @JvmStatic
    @JvmOverloads
    fun conversation(
      dispatcher: Dispatcher = createDispatcher(),
      store: VectorStore = LocalVectorStore(FromEnvironment.DEFAULT_EMBEDDING)
    ): PlatformConversation = Conversation(store, dispatcher)
  }
}

fun String.toOpenAIModel(token: String, host: String? = null, dispatcher: Dispatcher): OpenAIModel {
  val openAI = OpenAI(token, host, dispatcher)
  return openAI.supportedModels().find { it.name == this } ?: openAI.GPT_3_5_TURBO_16K
}