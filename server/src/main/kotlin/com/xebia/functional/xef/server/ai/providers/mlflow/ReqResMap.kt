package com.xebia.functional.xef.server.ai.providers.mlflow

import io.ktor.server.auth.*
import kotlinx.serialization.json.JsonObject

interface ReqResMap<FromRes, ToRes> {
  fun path(pathProvider: PathProvider, model: String, principal: UserIdPrincipal): String?

  fun mapRequest(from: JsonObject): JsonObject

  fun mapResponse(r1: FromRes): ToRes
}

object ChatReqResMap : ReqResMap<ChatResponse, OpenAIResponse> {

  private val mappedFields: Map<String, String> = mapOf("n" to "candidate_count")
  private val keys: Set<String> =
    setOf("messages", "temperature", "candidate_count", "stop", "max_tokens")

  override fun path(
    pathProvider: PathProvider,
    model: String,
    principal: UserIdPrincipal
  ): String? = pathProvider.chatPath(model, principal)

  override fun mapRequest(from: JsonObject): JsonObject =
    JsonObject(
      from.mapKeys { mappedFields.getOrDefault(it.key, it.key) }.filterKeys { keys.contains(it) }
    )

  override fun mapResponse(r1: ChatResponse): OpenAIResponse = r1.toOpenAI()
}

object EmbeddingsReqResMap : ReqResMap<EmbeddingsResponse, OpenAIEmbeddingResponse> {

  private val mappedFields: Map<String, String> = mapOf("input" to "text")
  private val keys = setOf("text")

  override fun path(
    pathProvider: PathProvider,
    model: String,
    principal: UserIdPrincipal
  ): String? = pathProvider.embeddingsPath(model, principal)

  override fun mapRequest(from: JsonObject): JsonObject =
    JsonObject(
      from.mapKeys { mappedFields.getOrDefault(it.key, it.key) }.filterKeys { keys.contains(it) }
    )

  override fun mapResponse(r1: EmbeddingsResponse): OpenAIEmbeddingResponse = r1.toOpenAI()
}
