package com.xebia.functional.xef.server.http.routes.providers.openai

import com.xebia.functional.xef.server.http.routes.providers.handleForwardToProvider
import io.ktor.client.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

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
