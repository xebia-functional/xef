package com.xebia.functional.xef.llm

import arrow.core.nonEmptyListOf
import com.xebia.functional.openai.infrastructure.ApiClient
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.env.getenv

private const val KEY_ENV_VAR = "OPENAI_TOKEN"
private const val HOST_ENV_VAR = "OPENAI_HOST"

fun <T : ApiClient> fromEnvironment(builder: (String) -> T): T {
  val token =
    getenv(KEY_ENV_VAR) ?: throw AIError.Env.OpenAI(nonEmptyListOf("missing $KEY_ENV_VAR env var"))
  val host = getenv(HOST_ENV_VAR)
  val api = builder(host ?: ApiClient.BASE_URL)
  api.setBearerToken(token)
  return api
}
