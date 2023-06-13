@file:JvmMultifileClass
@file:JvmName("Agent")

package com.xebia.functional.xef.auto

import arrow.core.nonFatalOrThrow
import arrow.core.raise.catch
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.append
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

@AiDsl
@JvmName("promptWithSerializer")
suspend fun <A> AIScope.prompt(
  prompt: Prompt,
  jsonSchema: String,
  serializer: (json: String) -> A,
  maxDeserializationAttempts: Int = 5,
  model: LLMModel = LLMModel.GPT_3_5_TURBO,
  user: String = "testing",
  echo: Boolean = false,
  n: Int = 1,
  temperature: Double = 0.0,
  bringFromContext: Int = 10,
  minResponseTokens: Int = 500,
): A {
  val responseInstructions =
    """
        |
        |Response Instructions: 
        |1. Return the entire response in a single line with not additional lines or characters.
        |2. When returning the response consider <string> values should be accordingly escaped so the json remains valid.
        |3. Use the JSON schema to produce the result exclusively in valid JSON format.
        |4. Pay attention to required vs non-required fields in the schema.
        |5. Escape any invalid JSON characters in the response.
        |JSON Schema:
        |$jsonSchema
        |Response:
        """
      .trimMargin()

  return tryDeserialize(serializer, maxDeserializationAttempts) {
    promptMessage(
      prompt.append(responseInstructions),
      model,
      user,
      echo,
      n,
      temperature,
      bringFromContext,
      minResponseTokens
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
