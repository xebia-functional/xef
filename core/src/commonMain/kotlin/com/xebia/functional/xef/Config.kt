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
  val logRequests: Boolean = false,
  val logResponses: Boolean = false
) {
  companion object {
    val DEFAULT = Config()
  }
}

private const val ORG_ENV_VAR = "OPENAI_ORG"
private const val HOST_ENV_VAR = "OPENAI_HOST"
private const val KEY_ENV_VAR = "OPENAI_TOKEN"

