package com.xebia.functional.xef.server.http.routes

import io.ktor.client.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.*
import kotlinx.serialization.json.JsonObject

fun Route.fineTuningRoutes(
    client: HttpClient,
) {
    post("/v1/files") {
        val multipartData = call.receiveMultipart()
        val parts = multipartData.readAllParts()
        val file = parts.filterIsInstance<PartData.FileItem>().find { it.name == "file" }
        val purpose = parts.filterIsInstance<PartData.FormItem>().find { it.name == "purpose" }

        handleForwardToProvider(client, call)
    }

    post("v1/fine_tuning/jobs") {
        val bodyJson = call.receive<JsonObject>()

        handleForwardToProvider(client, call)
    }

    get("v1/fine_tuning/jobs/{id}") {
        val id = call.parameters.getOrFail("id")

        handleForwardToProvider(client, call)
    }
}
