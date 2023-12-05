package com.xebia.functional.xef.mlflow.client

import com.xebia.functional.xef.client.ModelUriAdapter
import com.xebia.functional.xef.client.OpenAIPathType
import com.xebia.functional.xef.mlflow.MLflowRouteType
import com.xebia.functional.xef.mlflow.MlflowClient
import com.xebia.functional.xef.mlflow.RouteDefinition
import io.ktor.client.*

suspend fun mlflowGatewayConfig(
  gatewayUrl: String = "http://127.0.0.1:5000",
  select: (List<RouteDefinition>) -> RouteDefinition? = { it.firstOrNull() }
): (HttpClientConfig<*>) -> Unit {
  val client = MlflowClient(gatewayUrl)
  val routes: List<RouteDefinition> = client.use { it.searchRoutes() }
  val modelUriMap: Map<OpenAIPathType, Map<String, String>> =
    routes
      // Maps MLflowRouteType to OpenAIPathType
      .mapNotNull {
        when (it.routeType) {
          MLflowRouteType.CHAT -> OpenAIPathType.CHAT to it
          MLflowRouteType.EMBEDDINGS -> OpenAIPathType.EMBEDDINGS to it
          else -> null
        }
      }
      // Each OpenAIPathType is now associated to a list of routes
      .groupBy { it.first }
      // Now each model need to have one route definition associated to it
      .mapValues { it.value.map { (_, route) -> route }.selectRoutes(gatewayUrl, select) }

  val uriPathMap: Map<String, OpenAIPathType> =
    modelUriMap
      .toList()
      .flatMap { (pathType, modelUris) -> modelUris.toList().map { (_, uri) -> uri to pathType } }
      .toMap()

  val internalFun = { config: HttpClientConfig<*> ->
    config.install(ModelUriAdapter) { setPathMap(modelUriMap) }
    config.install(MLflowModelAdapter) { setPathMap(uriPathMap) }
  }
  return internalFun
}

private fun List<RouteDefinition>.selectRoutes(
  gatewayUrl: String,
  select: (List<RouteDefinition>) -> RouteDefinition?
): Map<String, String> =
  groupBy { it.model }
    // Use `select` to pick up the route from the list (by default the first one)
    .mapNotNull { (model, routes) ->
      select(routes)?.let { model.name to "$gatewayUrl${it.routeUrl}" }
    }
    .toMap()
