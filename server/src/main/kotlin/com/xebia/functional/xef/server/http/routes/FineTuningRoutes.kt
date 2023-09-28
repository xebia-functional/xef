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

/**
 * https://platform.openai.com/docs/api-reference/fine-tuning
 */
fun Route.oaiFineTuning(
    client: HttpClient,
) = route("v1/fine_tuning") {
    get("jobs") {
        // has query parameters btw
        val response = handleForwardToProvider(client)
    }

    post("jobs") {
        val bodyJson = call.receive<JsonObject>()

        val response = handleForwardToProvider(client)
    }

    get("jobs/{id}") {
        val id = call.parameters.getOrFail("id")

        val response = handleForwardToProvider(client)
    }

    post("jobs/{id}/cancel") {
        val id = call.parameters.getOrFail("id")

        val response = handleForwardToProvider(client)
    }

    get("jobs/{id}/events") {
        // has query parameters btw
        val id = call.parameters.getOrFail("id")

        val response = handleForwardToProvider(client)
    }
}

/**
 * https://platform.openai.com/docs/api-reference/files
 */
fun Route.oaiFiles(
    client: HttpClient,
) = route("v1/files") {
    get {
        val response = handleForwardToProvider(client)
    }

    post {
        val multipartData = call.receiveMultipart()
        val parts = multipartData.readAllParts()
        val file = parts.filterIsInstance<PartData.FileItem>().find { it.name == "file" }
        val purpose = parts.filterIsInstance<PartData.FormItem>().find { it.name == "purpose" }

        val response = handleForwardToProvider(client)
    }

    get("{id}") {
        val id = call.parameters.getOrFail("id")

        val response = handleForwardToProvider(client)
    }

    delete("{id}") {
        val id = call.parameters.getOrFail("id")

        val response = handleForwardToProvider(client)
    }

    get("{id}/content") {
        val id = call.parameters.getOrFail("id")

        val response = handleForwardToProvider(client)
    }
}