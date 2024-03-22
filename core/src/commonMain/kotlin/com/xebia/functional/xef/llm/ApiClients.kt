package com.xebia.functional.xef.llm

import arrow.core.nonEmptyListOf
import com.xebia.functional.openai.infrastructure.ApiClient
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.env.getenv

private const val KEY_ENV_VAR = "OPENAI_TOKEN"
private const val ORG_ENV_VAR = "OPENAI_ORG"
private const val HOST_ENV_VAR = "OPENAI_HOST"

fun <T : ApiClient> fromEnvironment(builder: (String, String?) -> T): T {
  val token =
    getenv(KEY_ENV_VAR) ?: throw AIError.Env.OpenAI(nonEmptyListOf("missing $KEY_ENV_VAR env var"))
  val org = getenv(ORG_ENV_VAR)
  return fromToken(token, org, builder)
}

fun <T : ApiClient> fromToken(
  token: String,
  org: String? = null,
  builder: (String, String?) -> T
): T {
  val host = getenv(HOST_ENV_VAR)
  val api = builder(host ?: ApiClient.BASE_URL, org)
  api.setBearerToken(token)
  return api
}
