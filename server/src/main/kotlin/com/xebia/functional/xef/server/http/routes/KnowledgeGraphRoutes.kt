package com.xebia.functional.xef.server.http.routes

import ai.xef.openai.OpenAIModel
import ai.xef.openai.StandardModel
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel.gpt_4_32k
import com.xebia.functional.xef.llm.models.modelType
import com.xebia.functional.xef.server.services.GraphStoreService
import com.xebia.functional.xef.store.GraphResponse
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.Accepted
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.knowledgeGraphRoutes(
  service: GraphStoreService
) {

  val configuredModel = StandardModel(gpt_4_32k)

  // Create a graph
  post("/graph/{id}") {
    call.parameters["id"]?.let { id ->
      // Logic to create a graph with the given id
      call.respondText("Graph with id $id created successfully", status = HttpStatusCode.Created)
    } ?: call.respondText("Missing or incorrect id", status = BadRequest)
  }

  // Query graph with Cypher queries
  post("/graph/{id}/query/{query}") {
    val id = call.parameters["id"]
    val query = call.parameters["query"]
    if (id != null && query != null) {
      // Logic to execute the Cypher query on the graph with the given id
      val graphStore = service.getGraphStore()
      // TODO do something with graph id
      val response: GraphResponse<Any?> = graphStore.executeQuery(query)
      call.respondText("Query result for graph $id: ${response.value}", status = HttpStatusCode.OK)
    } else {
      if (id == null) call.respondText("Missing or incorrect id", status = BadRequest)
      if (query == null) call.respondText("Missing or incorrect query", status = BadRequest)
    }
  }

  // Ingest large binary data via streaming
  post("/graph/{id}/ingest") {
    call.parameters["id"]?.let { id ->
      call.processIngestion(id, configuredModel)
    } ?: call.respondText("Missing or incorrect id", status = BadRequest)
  }
}

suspend fun ApplicationCall.processIngestion(id: String, configuredModel: OpenAIModel<*>) {
  receiveMultipart().forEachPart { part ->
    when (part) {
      is PartData.FileItem -> {
        processFile(configuredModel, part)
      }
      // Handle other part types if necessary
      else -> part.dispose()
    }
  }
  respondText("Data ingested successfully into graph $id", status = Accepted)
}

private fun processFile(
  configuredModel: OpenAIModel<*>,
  part: PartData.FileItem
) {
  part.streamProvider().use { inputStream ->
    val modelType = configuredModel.modelType(forFunctions = true)
    val modelContextLength = modelType.maxContextLength
    // reserve 75% of the model's max context length for the prompt
    val promptContextLength = (modelContextLength * 0.75).toInt()
    val buffer = StringBuilder()
    val reader = inputStream.bufferedReader()
    // consume up to the prompt context length from the input stream
    while (buffer.length < promptContextLength) {
      val content = buffer.toString()
      val bufferTokenSize = modelType.encoding.countTokens(content)
      if (bufferTokenSize >= promptContextLength) {
        // send the buffer for processing
        // TODO send buffer for processing and stream back generated migration=
        // reset the buffer
        buffer.clear()
      } else {
        val line = reader.readLine() ?: break
        buffer.append(line)
      }
    }
  }
  part.dispose() // Make sure to dispose of the part after use
}