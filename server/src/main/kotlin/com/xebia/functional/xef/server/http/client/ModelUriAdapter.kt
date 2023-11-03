package com.xebia.functional.xef.server.http.client

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class ModelUriAdapter internal constructor(private val urlMap: Map<OpenAIPathType, Map<String, String>>) {

    val logger = KotlinLogging.logger {}

    fun isDefined(path: OpenAIPathType): Boolean = urlMap.containsKey(path)

    fun findPath(path: OpenAIPathType, model: String): String? = urlMap[path]?.get(model)

    companion object : HttpClientPlugin<ModelUriAdapterBuilder, ModelUriAdapter> {

        override val key: AttributeKey<ModelUriAdapter> = AttributeKey("ModelAuthAdapter")

        override fun prepare(block: ModelUriAdapterBuilder.() -> Unit): ModelUriAdapter =
            ModelUriAdapterBuilder().apply(block).build()

        override fun install(plugin: ModelUriAdapter, scope: HttpClient) {
            installModelUriAdapter(plugin, scope)
        }

        private fun readModelFromRequest(originalRequest: ByteArray): String? {
            val requestBody = originalRequest.toString(Charsets.UTF_8)
            val json = Json.decodeFromString<JsonObject>(requestBody)
            return json["model"]?.jsonPrimitive?.content
        }

        private fun installModelUriAdapter(plugin: ModelUriAdapter, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.Transform) { content ->
                val originalPath = OpenAIPathType.from(context.url.encodedPath) ?: return@intercept
                if (!plugin.isDefined(originalPath)) return@intercept
                val model = when (content) {
                    is OutgoingContent.ByteArrayContent -> readModelFromRequest(content.bytes())
                    is ByteArray -> readModelFromRequest(content)
                    else -> return@intercept
                }
                val newURL = model?.let { plugin.findPath(originalPath, it) }
                if (newURL == null)  plugin.logger.info { "New url for path $originalPath and model $model not found" }
                else {
                    plugin.logger.info { "Intercepting request for path $originalPath and model $model to $newURL" }
                    val baseBuilder = URLBuilder(newURL).build()
                    context.url.set(
                        scheme = baseBuilder.protocol.name,
                        host = baseBuilder.host,
                        port = baseBuilder.port,
                        path = baseBuilder.encodedPath
                    )
                }
            }
        }

    }
}