package com.xebia.functional.xef.conversation.llm.openai

import arrow.core.nonEmptyListOf
import com.aallam.openai.api.exception.InvalidRequestException
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI as OpenAIClient
import com.aallam.openai.client.OpenAIHost
import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.AutoClose
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.PlatformConversation
import com.xebia.functional.xef.conversation.autoClose
import com.xebia.functional.xef.conversation.llm.openai.models.*
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.llm.LLM
import com.xebia.functional.xef.llm.models.MaxIoContextLength
import com.xebia.functional.xef.llm.models.ModelID
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
    OpenAIHost.OpenAI.baseUrl
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

  internal val defaultClient =
    OpenAIClient(
        host = getHost()?.let { OpenAIHost(it) } ?: OpenAIHost.OpenAI,
        token = getToken(),
        logging = LoggingConfig(LogLevel.None),
        headers = mapOf("Authorization" to " Bearer ${getToken()}"),
      )
      .let { autoClose(it) }

  val GPT_4 by lazy {
    OpenAIChat(
        provider = this,
        modelID = ModelID("gpt-4"),
        contextLength = MaxIoContextLength.Combined(8192),
        encodingType = EncodingType.CL100K_BASE
      )
      .autoCloseBind()
  }

  val GPT_4_0314 by lazy {
    OpenAIFunChat(
        provider = this,
        modelID = ModelID("gpt-4-0314"),
        contextLength = MaxIoContextLength.Combined(4097),
        encodingType = EncodingType.CL100K_BASE
      )
      .autoCloseBind() // legacy
  }

  val GPT_4_32K by lazy {
    OpenAIChat(
        provider = this,
        modelID = ModelID("gpt-4-32k"),
        contextLength = MaxIoContextLength.Combined(32768),
        encodingType = EncodingType.CL100K_BASE
      )
      .autoCloseBind()
  }

  val GPT_3_5_TURBO by lazy {
    OpenAIChat(
        provider = this,
        modelID = ModelID("gpt-3.5-turbo"),
        contextLength = MaxIoContextLength.Combined(4097),
        encodingType = EncodingType.CL100K_BASE
      )
      .autoCloseBind()
  }

  val GPT_3_5_TURBO_16K by lazy {
    OpenAIChat(
        provider = this,
        modelID = ModelID("gpt-3.5-turbo-16k"),
        contextLength = MaxIoContextLength.Combined(4097 * 4),
        encodingType = EncodingType.CL100K_BASE
      )
      .autoCloseBind()
  }

  val GPT_3_5_TURBO_FUNCTIONS by lazy {
    OpenAIFunChat(
        provider = this,
        modelID = ModelID("gpt-3.5-turbo-0613"),
        contextLength = MaxIoContextLength.Combined(4097),
        encodingType = EncodingType.CL100K_BASE
      )
      .autoCloseBind()
  }

  val GPT_3_5_TURBO_0301 by lazy {
    OpenAIChat(
        provider = this,
        modelID = ModelID("gpt-3.5-turbo-0301"),
        contextLength = MaxIoContextLength.Combined(4097),
        encodingType = EncodingType.CL100K_BASE
      )
      .autoCloseBind() // legacy
  }

  val TEXT_DAVINCI_003 by lazy {
    OpenAICompletion(
        provider = this,
        modelID = ModelID("text-davinci-003"),
        contextLength = MaxIoContextLength.Combined(4097),
        encodingType = EncodingType.P50K_BASE
      )
      .autoCloseBind()
  }

  val TEXT_DAVINCI_002 by lazy {
    OpenAICompletion(
        provider = this,
        modelID = ModelID("text-davinci-002"),
        contextLength = MaxIoContextLength.Combined(4097),
        encodingType = EncodingType.P50K_BASE
      )
      .autoCloseBind()
  }

  val TEXT_CURIE_001 by lazy {
    OpenAICompletion(
        provider = this,
        modelID = ModelID("text-similarity-curie-001"),
        contextLength = MaxIoContextLength.Combined(2049),
        encodingType = EncodingType.P50K_BASE
      )
      .autoCloseBind()
  }

  val TEXT_BABBAGE_001 by lazy {
    OpenAICompletion(
        provider = this,
        modelID = ModelID("text-babbage-001"),
        contextLength = MaxIoContextLength.Combined(2049),
        encodingType = EncodingType.P50K_BASE
      )
      .autoCloseBind()
  }

  val TEXT_ADA_001 by lazy {
    OpenAICompletion(
        provider = this,
        modelID = ModelID("text-ada-001"),
        contextLength = MaxIoContextLength.Combined(2049),
        encodingType = EncodingType.P50K_BASE
      )
      .autoCloseBind()
  }

  val TEXT_EMBEDDING_ADA_002 by lazy {
    OpenAIEmbeddings(
        provider = this,
        modelID = ModelID("text-embedding-ada-002"),
        encodingType = EncodingType.CL100K_BASE
      )
      .autoCloseBind()
  }

  val DALLE_2 by lazy {
    OpenAIImages(
        provider = this,
        modelID = ModelID("dalle-2"),
        encodingType = EncodingType.CL100K_BASE
      )
      .autoCloseBind()
  }

  @JvmField val DEFAULT_CHAT = GPT_3_5_TURBO_16K

  @JvmField val DEFAULT_SERIALIZATION = GPT_3_5_TURBO_FUNCTIONS

  @JvmField val DEFAULT_EMBEDDING = TEXT_EMBEDDING_ADA_002

  @JvmField val DEFAULT_IMAGES = DALLE_2

  fun supportedModels(): List<LLM> = // TODO: impl of abstract provider function
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

  suspend fun findModel(modelId: String): ModelID? { // TODO: impl of abstract provider function
    val model =
      try {
        defaultClient.model(ModelId(modelId))
      } catch (e: InvalidRequestException) {
        when (e.error.detail?.code) {
          "model_not_found" -> return null
          else -> throw e
        }
      }
    return ModelID(model.id.id)
  }

  suspend fun <T : LLM> spawnModel(
    modelId: String,
    baseModel: T
  ): T { // TODO: impl of abstract provider function
    if (findModel(modelId) == null) error("model not found")
    return baseModel.copy(ModelID(modelId)) as? T
      ?: error("${baseModel::class} does not follow contract to return the most specific type")
  }

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
