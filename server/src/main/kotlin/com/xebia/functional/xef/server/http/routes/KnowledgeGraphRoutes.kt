package com.xebia.functional.xef.server.http.routes

import ai.xef.openai.OpenAIModel
import ai.xef.openai.StandardModel
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.llm.models.modelType
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.knowledgeGraphRoutes() {

  val configuredModel = StandardModel(CreateChatCompletionRequestModel.gpt_4_32k)


  // Create a graph
  post("/graph/{id}") {
    call.parameters["id"]?.let { id ->
      // Logic to create a graph with the given id
      call.respondText("Graph with id $id created successfully", status = HttpStatusCode.Created)
    } ?: call.respondText("Missing or incorrect id", status = HttpStatusCode.BadRequest)
  }

  // Query graph with Cypher queries
  post("/graph/{id}/query/{query}") {
    val id = call.parameters["id"]
    val query = call.parameters["query"]
    if (id != null && query != null) {
      // Logic to execute the Cypher query on the graph with the given id
      call.respondText("Query result for graph $id: [query result here]", status = HttpStatusCode.OK)
    } else {
      if (id == null) call.respondText("Missing or incorrect id", status = HttpStatusCode.BadRequest)
      if (query == null) call.respondText("Missing or incorrect query", status = HttpStatusCode.BadRequest)
    }
  }

  // Ingest large binary data via streaming
  post("/graph/{id}/ingest") {
    call.parameters["id"]?.let { id ->
      processIngestion(call, id, configuredModel)
    } ?: call.respondText("Missing or incorrect id", status = HttpStatusCode.BadRequest)
  }
}

suspend fun processIngestion(call: ApplicationCall, id: String, configuredModel: OpenAIModel<*>) {
  val multipart = call.receiveMultipart()
  multipart.forEachPart { part ->
    when (part) {
      is PartData.FileItem -> {
        processFile(configuredModel, part)
      }
      // Handle other part types if necessary
      else -> part.dispose()
    }
  }
  call.respondText("Data ingested successfully into graph $id", status = HttpStatusCode.Accepted)
}

private fun processFile(
  configuredModel: OpenAIModel<*>,
  part: PartData.FileItem
) {
  val modelType = configuredModel.modelType(forFunctions = true)
  val modelContextLength = modelType.maxContextLength
  // reserve 75% of the model's max context length for the prompt
  val promptContextLength = (modelContextLength * 0.75).toInt()
  val buffer = StringBuilder()
  part.streamProvider().use { inputStream ->
    val reader = inputStream.bufferedReader()
    // consume up to the prompt context length from the input stream
    while (buffer.length < promptContextLength) {
      val bufferTokenSize = modelType.encoding.countTokens(buffer.toString())
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
