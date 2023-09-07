package com.xebia.functional.xef.conversation.llm.openai

import arrow.core.nonEmptyListOf
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI as OpenAIClient
import com.aallam.openai.client.OpenAIHost
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.AutoClose
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.PlatformConversation
import com.xebia.functional.xef.conversation.autoClose
import com.xebia.functional.xef.conversation.llm.openai.models.*
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.llm.LLM
import com.xebia.functional.xef.store.LocalVectorStore
import com.xebia.functional.xef.store.VectorStore
import kotlin.jvm.JvmField
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

private const val KEY_ENV_VAR = "OPENAI_TOKEN"
private const val HOST_ENV_VAR = "OPENAI_HOST"

class OpenAI(internal var token: String? = null, internal var host: String? = null) :
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

  fun createClient() =
    OpenAIClient(
      host = getHost()?.let { OpenAIHost(it) } ?: OpenAIHost.OpenAI,
      token = getToken(),
      logging = LoggingConfig(LogLevel.None),
      headers = mapOf("Authorization" to " Bearer ${getToken()}"),
    )

  val GPT_4 by lazy { autoClose(OpenAIFunChat(ModelType.GPT_4, createClient())) }

  val GPT_4_0314 by lazy {
    autoClose(OpenAIFunChat(ModelType.GPT_4, createClient())) // legacy
  }

  val GPT_4_32K by lazy { autoClose(OpenAIFunChat(ModelType.GPT_4_32K, createClient())) }

  val GPT_3_5_TURBO by lazy { autoClose(OpenAIChat(ModelType.GPT_3_5_TURBO, createClient())) }

  val GPT_3_5_TURBO_16K by lazy {
    autoClose(OpenAIChat(ModelType.GPT_3_5_TURBO_16_K, createClient()))
  }

  val GPT_3_5_TURBO_FUNCTIONS by lazy {
    autoClose(OpenAIFunChat(ModelType.GPT_3_5_TURBO_FUNCTIONS, createClient()))
  }

  val GPT_3_5_TURBO_0301 by lazy {
    autoClose(OpenAIChat(ModelType.GPT_3_5_TURBO, createClient())) // legacy
  }

  val TEXT_DAVINCI_003 by lazy {
    autoClose(OpenAICompletion(ModelType.TEXT_DAVINCI_003, createClient()))
  }

  val TEXT_DAVINCI_002 by lazy {
    autoClose(OpenAICompletion(ModelType.TEXT_DAVINCI_002, createClient()))
  }

  val TEXT_CURIE_001 by lazy {
    autoClose(OpenAICompletion(ModelType.TEXT_SIMILARITY_CURIE_001, createClient()))
  }

  val TEXT_BABBAGE_001 by lazy {
    autoClose(OpenAICompletion(ModelType.TEXT_BABBAGE_001, createClient()))
  }

  val TEXT_ADA_001 by lazy { autoClose(OpenAICompletion(ModelType.TEXT_ADA_001, createClient())) }

  val TEXT_EMBEDDING_ADA_002 by lazy {
    autoClose(OpenAIEmbeddings(ModelType.TEXT_EMBEDDING_ADA_002, createClient()))
  }

  val DALLE_2 by lazy { autoClose(OpenAIImages(ModelType.GPT_3_5_TURBO, createClient())) }

  @JvmField val DEFAULT_CHAT = GPT_3_5_TURBO_16K

  @JvmField val DEFAULT_SERIALIZATION = GPT_3_5_TURBO_FUNCTIONS

  @JvmField val DEFAULT_EMBEDDING = TEXT_EMBEDDING_ADA_002

  @JvmField val DEFAULT_IMAGES = DALLE_2

  fun supportedModels(): List<LLM> =
    listOf(
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
      DALLE_2,
    )

  companion object {

    @JvmField val FromEnvironment: OpenAI = OpenAI()

    @JvmSynthetic
    suspend inline fun <A> conversation(
      store: VectorStore,
      noinline block: suspend Conversation.() -> A
    ): A = block(conversation(store))

    @JvmSynthetic
    suspend fun <A> conversation(block: suspend Conversation.() -> A): A =
      block(conversation(LocalVectorStore(FromEnvironment.DEFAULT_EMBEDDING)))

    @JvmStatic
    @JvmOverloads
    fun conversation(
      store: VectorStore = LocalVectorStore(FromEnvironment.DEFAULT_EMBEDDING)
    ): PlatformConversation = Conversation(store)
  }
}
