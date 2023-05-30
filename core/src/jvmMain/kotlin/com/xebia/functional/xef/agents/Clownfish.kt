package com.xebia.functional.xef.agents

import com.xebia.functional.xef.auto.AIScope
import com.xebia.functional.xef.auto.prompt
import com.xebia.functional.xef.auto.serialization.buildJsonSchema
import com.xebia.functional.xef.llm.openai.LLMModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

suspend inline fun <reified A> AIScope.clownfish(
  question: String,
  model: LLMModel = LLMModel.GPT_3_5_TURBO
): A = clownfish(question, model, serializer())

suspend inline fun <reified A> AIScope.clownfish(
  question: String,
  model: LLMModel = LLMModel.GPT_3_5_TURBO,
  serializer: KSerializer<A>,
): A {
  val jsonSchema: JsonObject = buildJsonSchema(serializer.descriptor, false)
  val prompt: String = jsonExpertPrompt(jsonSchema, question)

  val result: A = prompt(prompt)

  return result
}

fun jsonExpertPrompt(jsonSchema: JsonObject, question: String): String =
  """
        |You are a JSON Expert, parsing the values of answering a question, and meeting a JSON Schema.
        |Response Instructions: 
        |1. Return the entire response in a single line with not additional lines or characters.
        |2. When returning the response consider <string> values should be accordingly escaped so the json remains valid.
        |3. Use the JSON schema to produce the result exclusively in valid JSON format.
        |4. Pay attention to required vs non-required fields in the schema.
        |5. Start the response with {
        |JSON Schema:
        |${jsonSchema}
        |Question:
        |${question}
        |Response:
        """
    .trimMargin()
