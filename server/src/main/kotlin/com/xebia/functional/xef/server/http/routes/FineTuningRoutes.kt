package com.xebia.functional.xef.server.http.routes

import io.ktor.client.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.serialization.json.JsonObject

fun Routing.fineTuningRoutes(
    client: HttpClient,
) {
    val openAiUrl = "https://api.openai.com/v1"

    authenticate("auth-bearer") {
        post("/v1/files") {
            val bodyBytes = call.receiveChannel().toByteArray()

            val multipartData = call.receiveMultipart()
            val parts = multipartData.readAllParts()
            val file = parts.filterIsInstance<PartData.FileItem>().find { it.name == "file" }
            val purpose = parts.filterIsInstance<PartData.FormItem>().find { it.name == "purpose" }

            client.makeRequest2(call, "$openAiUrl/files", bodyBytes)
        }

        post("v1/fine_tuning/jobs") {
            val bodyBytes = call.receiveChannel().toByteArray()
            val bodyJson = call.receive<JsonObject>()

            println(bodyJson)

            client.makeRequest2(call, "$openAiUrl/fine_tuning/jobs", bodyBytes)
        }

        post("v1/fine_tuning/jobs/{id}") {
            val bodyBytes = call.receiveChannel().toByteArray()
            val bodyJson = call.receive<JsonObject>()
            call.request.path()

            println(bodyJson)

            client.makeRequest2(call, "$openAiUrl/fine_tuning/jobs", bodyBytes)
        }
    }
}
