package com.xebia.functional.xef.server.http.client.mlflow

import com.xebia.functional.xef.server.http.client.ModelUriAdapter
import com.xebia.functional.xef.server.http.client.OpenAIPathType
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MLflowModelAdapter internal constructor(private val mappedRequests: Map<String, OpenAIPathType>) {

    val logger = KotlinLogging.logger {}

    fun mappedType(path: String): OpenAIPathType? = mappedRequests[path]

    companion object : HttpClientPlugin<MLflowModelAdapterBuilder, MLflowModelAdapter> {

        @OptIn(ExperimentalSerializationApi::class)
        private val json = Json {
            explicitNulls = false
            ignoreUnknownKeys = true
        }

        override val key: AttributeKey<MLflowModelAdapter> = AttributeKey("MLflowAdapter")

        override fun prepare(block: MLflowModelAdapterBuilder.() -> Unit): MLflowModelAdapter =
            MLflowModelAdapterBuilder().apply(block).build()

        override fun install(plugin: MLflowModelAdapter, scope: HttpClient) {
            installMLflowRequestAdapter(plugin, scope)
            installMLflowResponseAdapter(plugin, scope)
        }

        private fun installMLflowRequestAdapter(plugin: MLflowModelAdapter, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.Transform) { content ->
                val originalPath = plugin.mappedType(context.url.buildString()) ?: return@intercept
                val originalRequest = when (content) {
                    is OutgoingContent.ByteArrayContent -> content.bytes()
                    is ByteArray -> content
                    else -> return@intercept
                }
                when (originalPath) {
                    OpenAIPathType.CHAT -> TODO()
                    OpenAIPathType.EMBEDDINGS -> {
                        plugin.logger.info { "Intercepting request for path $originalPath" }
                        val stringRequestBody = originalRequest.toString(Charsets.UTF_8)
                        val requestData = json.decodeFromString<XefEmbeddingsRequest>(stringRequestBody)
                        val newRequest = TextContent(json.encodeToString(requestData.toMLflow()), ContentType.Application.Json)
                        proceedWith(newRequest)
                    }
                    else -> {
                        plugin.logger.warn { "$originalPath not supported" }
                        return@intercept
                    }
                }
            }
        }

        private fun installMLflowResponseAdapter(plugin: MLflowModelAdapter, scope: HttpClient) {
            scope.responsePipeline.intercept(HttpResponsePipeline.Transform) { content ->
                val originalPath = plugin.mappedType(context.request.url.toString()) ?: return@intercept
                val contentResponse = content.response as? ByteReadPacket ?: return@intercept
                when (originalPath) {
                    OpenAIPathType.CHAT -> TODO()
                    OpenAIPathType.EMBEDDINGS -> {
                        val stringResponseBody = contentResponse.readText()
                        val responseData = json.decodeFromString<MLflowEmbeddingsResponse>(stringResponseBody)
                        val newResponse = ByteReadPacket(json.encodeToString(responseData.toXef()).toByteArray(Charsets.UTF_8))
                        val response = HttpResponseContainer(content.expectedType, newResponse)
                        proceedWith(response)
                    }
                    else -> {
                        plugin.logger.warn { "$originalPath not supported" }
                        return@intercept
                    }
                }
            }
        }
    }

}