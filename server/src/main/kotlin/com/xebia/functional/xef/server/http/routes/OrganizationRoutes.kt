package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.xef.server.models.OrganizationRequest
import com.xebia.functional.xef.server.services.OrganizationRepositoryService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Routing.organizationRoutes(
    orgRepositoryService: OrganizationRepositoryService
) {
    authenticate("auth-bearer") {
        post("/v1/settings/org") {
            try {
                val request = Json.decodeFromString<OrganizationRequest>(call.receive<String>())
                val token = call.principal<UserIdPrincipal>()
                val response = orgRepositoryService.createOrganization(request, token?.name)
                call.respond(
                    status = HttpStatusCode.Created,
                    response
                )
            } catch (e: Exception) {
                call.respondText(e.message ?: "Unexpected error", status = HttpStatusCode.BadRequest)
            }
        }
    }
}
