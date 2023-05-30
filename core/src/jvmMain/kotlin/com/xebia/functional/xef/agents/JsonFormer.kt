package com.xebia.functional.xef.agents

import arrow.core.fold
import arrow.core.raise.catch
import com.xebia.functional.tokenizer.TokenVocabulary
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.AIScope
import com.xebia.functional.xef.auto.buildLogitBias
import com.xebia.functional.xef.auto.prompt
import com.xebia.functional.xef.auto.serialization.buildJsonSchema
import com.xebia.functional.xef.llm.openai.LLMModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer

@Serializable data class Book(val title: String, val author: String, val summary: String)

suspend inline fun <reified A> AIScope.jsonFormerPrompt(
  question: String,
  model: LLMModel = LLMModel.GPT_3_5_TURBO
): A = jsonFormerPrompt(question, model, serializer())

suspend inline fun <reified A> AIScope.jsonFormerPrompt(
  question: String,
  model: LLMModel = LLMModel.GPT_3_5_TURBO,
  serializer: KSerializer<A>,
): A {
  val jsonSchema: JsonObject = buildJsonSchema(serializer.descriptor, false)
  val prompt: String = question + jsonSchemaPrompt(jsonSchema)

  catch({
    val properties: JsonObject = properties(jsonSchema).jsonObject
    val json: JsonObject =
      properties.fold(JsonObject(mapOf())) { acc, entries ->
        addMarkerToJsonElement(
          question = prompt,
          model = model,
          key = entries.key,
          jsonElement = entries.value,
          json = acc
        )
      }
    println(json)
  }) {
    AIError.NoResponse
  }

  return prompt(question)
}

fun AIScope.properties(json: JsonObject): JsonElement =
  json["properties"] ?: raise(AIError.NoResponse)

suspend fun AIScope.addMarkerToJsonElement(
  question: String,
  model: LLMModel,
  key: String,
  jsonElement: JsonElement,
  json: JsonObject,
  tokenVocabulary: TokenVocabulary = TokenVocabulary(model.modelType.encodingType)
): JsonObject {
  return when (jsonElement) {
    is JsonObject -> {
      jsonElement.fold(json) { acc, entry ->
        addMarkerToJsonElement(
          question = question,
          model = model,
          key = key,
          jsonElement = entry.value,
          json = acc
        )
      }
    }
    is JsonArray -> {
      JsonObject(mapOf(key to jsonElement))
    }
    is JsonPrimitive -> {
      when (jsonElement.content) {
        "string" -> {
          val regex = Regex("[a-zA-Z0-9 -_]+")
          val preCompletion: Map<String, JsonElement> =
            json + mapOf(key to JsonPrimitive("|GENERATION|"))
          val prompt: String = question + partialJsonPrompt(JsonObject(preCompletion))

          val logitBias: Map<Int, Int> =
            tokenVocabulary.buildLogitBias(
                forbiddenKeys = listOf("\""),
                limitBias = 1
            )

          val partialCompletion: String =
            patternPrompt(
              prompt,
              regex,
              model = model,
              maxIterations = 3,
              maxTokensPerCompletion = 3,
              logitBias = logitBias
            )
          JsonObject(json + mapOf(key to JsonPrimitive(partialCompletion)))
        }
        "number" -> {
          val regex = Regex("[0-9]+")
          val partialCompletion: String = patternPrompt(question, regex, maxTokensPerCompletion = 6)
          JsonObject(json + mapOf(key to JsonPrimitive(partialCompletion)))
        }
        "array" -> {
          JsonObject(mapOf())
        }
        "object" -> {
          JsonObject(mapOf())
        }
        else -> raise(AIError.NoResponse)
      }
    }
  }
}

fun jsonSchemaPrompt(jsonSchema: JsonObject): String =
  """
        |
        |Response Instructions: 
        |1. Return only and only the content that should be inside the |GENERATION| tag of the Partial JSON.
        |2. Return the entire response in a single line with not additional lines or characters.
        |3. Make sure the value meets the type of the JSON Schema
        |4. Pay attention to required vs non-required fields in the schema.
        |5. Do not start the response with " or any special character.
        |JSON Schema:
        |${jsonSchema}
        """
    .trimMargin()

fun partialJsonPrompt(partialJson: JsonObject): String =
  """
        |
        |Partial JSON:
        |${partialJson}
        |Response:
    """
    .trimMargin()
