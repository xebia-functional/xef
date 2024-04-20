package com.xebia.functional.xef

import com.xebia.functional.xef.env.getenv
import kotlinx.serialization.json.Json

data class Config(
  val baseUrl: String = getenv(HOST_ENV_VAR) ?: "https://api.openai.com/v1/",
  val token: String? = null,
  val org: String? = getenv(ORG_ENV_VAR),
  val json: Json = Json {
    ignoreUnknownKeys = true
    prettyPrint = false
    isLenient = true
    explicitNulls = false
    classDiscriminator = "_type_"
  },
  val streamingPrefix: String = "data:",
  val streamingDelimiter: String = "data: [DONE]"
) {
  companion object {
    val DEFAULT = Config()
  }
}

internal const val ORG_ENV_VAR = "OPENAI_ORG"
internal const val HOST_ENV_VAR = "OPENAI_HOST"
internal const val KEY_ENV_VAR = "OPENAI_TOKEN"

/**
 * Constructor that mimics the behavior of "ApiClient", but without the additional layer in between.
 * Just simple fun on top of generated API.
 */

