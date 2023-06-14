@file:JvmMultifileClass
@file:JvmName("Agent")

package com.xebia.functional.xef.auto

import arrow.core.nonFatalOrThrow
import arrow.core.raise.catch
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.openai.functions.CFunction
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.prompt.Prompt
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

@AiDsl
@JvmName("promptWithSerializer")
suspend fun <A> AIScope.prompt(
  prompt: Prompt,
  functions: List<CFunction>,
  serializer: (json: String) -> A,
  maxDeserializationAttempts: Int = 5,
  model: LLMModel = LLMModel.GPT_3_5_TURBO_FUNCTIONS,
  user: String = "testing",
  echo: Boolean = false,
  n: Int = 1,
  temperature: Double = 0.0,
  bringFromContext: Int = 10,
  minResponseTokens: Int = 500,
): A {
  return tryDeserialize(serializer, maxDeserializationAttempts) {
    promptMessage(
      prompt = prompt,
      model = model,
      functions = functions,
      user = user,
      echo = echo,
      n = n,
      temperature = temperature,
      bringFromContext = bringFromContext,
      minResponseTokens = minResponseTokens
    )
  }
}

suspend fun <A> AIScope.tryDeserialize(
  serializer: (json: String) -> A,
  maxDeserializationAttempts: Int,
  agent: AI<List<String>>
): A {
  (0 until maxDeserializationAttempts).forEach { currentAttempts ->
    val result = agent().firstOrNull() ?: throw AIError.NoResponse()
    catch({
      return@tryDeserialize serializer(result)
    }) { e: Throwable ->
      if (currentAttempts == maxDeserializationAttempts)
        throw AIError.JsonParsing(result, maxDeserializationAttempts, e.nonFatalOrThrow())
      // TODO else log attempt ?
    }
  }
  throw AIError.NoResponse()
}
