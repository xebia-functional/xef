package com.xebia.functional.xef.server.ai.providers.mlflow

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.Logger

interface PathProvider {
    fun chatPath(model: String, principal: UserIdPrincipal): String?
    fun embeddingsPath(model: String, principal: UserIdPrincipal): String?
}

@Serializable
data class RoutesResponse(
    val routes: List<RouteDefinition>
)

@Serializable
data class RouteDefinition(
    val name: String,
    @SerialName("route_type")
    val routeType: String,
    val model: RouteModel,
    @SerialName("route_url")
    val routeUrl: String,
)

@Serializable
data class RouteModel(
    val name: String,
    val provider: String,
)

private val json = Json { ignoreUnknownKeys = true }

suspend fun mlflowPathProvider(gatewayUrl: String, client: HttpClient): PathProvider {
    val response = client.get("$gatewayUrl/api/2.0/gateway/routes/")
    if (response.status.isSuccess()) {
        val textResponse = response.bodyAsText()
        val data = json.decodeFromString<RoutesResponse>(textResponse)
        val map = data.routes.groupBy { it.model.name }
        return object : PathProvider {
            override fun chatPath(model: String, principal: UserIdPrincipal): String? =
                map[model]?.firstOrNull { it.routeType == "llm/v1/chat" }?.routeUrl
            override fun embeddingsPath(model: String, principal: UserIdPrincipal): String? =
                map[model]?.firstOrNull { it.routeType == "llm/v1/embeddings" }?.routeUrl

        }
    }
    else throw IllegalStateException("Received ${response.status} code from MLflow Gateway")
}