package com.xebia.functional.xef.server.http.client

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class ModelUriAdapter
internal constructor(private val urlMap: Map<OpenAIPathType, Map<String, String>>) {

  val logger = KotlinLogging.logger {}

  fun isDefined(path: OpenAIPathType): Boolean = urlMap.containsKey(path)

  fun findPath(path: OpenAIPathType, model: String): String? = urlMap[path]?.get(model)

  companion object : HttpClientPlugin<ModelUriAdapterBuilder, ModelUriAdapter> {

    override val key: AttributeKey<ModelUriAdapter> = AttributeKey("ModelAuthAdapter")

    override fun prepare(block: ModelUriAdapterBuilder.() -> Unit): ModelUriAdapter =
      ModelUriAdapterBuilder().apply(block).build()

    override fun install(plugin: ModelUriAdapter, scope: HttpClient) {
      installModelAuthAdapter(plugin, scope)
    }

    private fun readModelFromRequest(originalRequest: OutgoingContent.ByteArrayContent?): String? {
      val requestBody = originalRequest?.bytes()?.toString(Charsets.UTF_8)
      val json = requestBody?.let { Json.decodeFromString<JsonObject>(it) }
      return json?.get("model")?.jsonPrimitive?.content
    }

    private fun installModelAuthAdapter(plugin: ModelUriAdapter, scope: HttpClient) {
      val adaptAuthRequestPhase = PipelinePhase("ModelAuthAdaptRequest")
      scope.sendPipeline.insertPhaseAfter(HttpSendPipeline.State, adaptAuthRequestPhase)
      scope.sendPipeline.intercept(adaptAuthRequestPhase) { content ->
        val originalPath = OpenAIPathType.from(context.url.encodedPath) ?: return@intercept
        if (plugin.isDefined(originalPath)) {
          val originalRequest = content as? OutgoingContent.ByteArrayContent
          if (originalRequest == null) {
            plugin.logger.warn {
              """
                        |Can't adapt the model auth. 
                        |The body type is: ${content::class}, with Content-Type: ${context.contentType()}.
                        |
                        |If you expect serialized body, please check that you have installed the corresponding 
                        |plugin(like `ContentNegotiation`) and set `Content-Type` header."""
                .trimMargin()
            }
            return@intercept
          }
          val model = readModelFromRequest(originalRequest)
          val newURL = model?.let { plugin.findPath(originalPath, it) }
          if (newURL == null) {
            plugin.logger.info {
              "Model auth didn't found a new url for path $originalPath and model $model"
            }
          } else {
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
}
