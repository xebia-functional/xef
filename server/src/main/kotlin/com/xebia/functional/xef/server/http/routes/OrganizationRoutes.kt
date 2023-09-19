package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.xef.server.models.OrganizationRequest
import com.xebia.functional.xef.server.models.OrganizationUpdateRequest
import com.xebia.functional.xef.server.models.exceptions.XefExceptions
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
        get("/v1/settings/org") {
            val token = call.getToken()
            val response = orgRepositoryService.getOrganizations(token)
            call.respond(response)
        }
        get("/v1/settings/org/{id}") {

            val token = call.getToken()
            val id = call.getId()
            val response = orgRepositoryService.getOrganization(token, id)
            call.respond(response)
        }
        get("/v1/settings/org/{id}/users") {
            val token = call.getToken()
            val id = call.getId()
            val response = orgRepositoryService.getUsersInOrganization(token, id)
            call.respond(response)
        }
        post("/v1/settings/org") {

            val request = Json.decodeFromString<OrganizationRequest>(call.receive<String>())
            val token = call.getToken()
            val response = orgRepositoryService.createOrganization(request, token)
            call.respond(
                status = HttpStatusCode.Created,
                response
            )
        }
        put("/v1/settings/org/{id}") {
            val request = Json.decodeFromString<OrganizationUpdateRequest>(call.receive<String>())
            val token = call.getToken()
            val id = call.getId()
            val response = orgRepositoryService.updateOrganization(token, request, id)
            call.respond(
                status = HttpStatusCode.NoContent,
                response
            )
        }
        delete("/v1/settings/org/{id}") {
            val token = call.getToken()
            val id = call.getId()
            val response = orgRepositoryService.deleteOrganization(token, id)
            call.respond(
                status = HttpStatusCode.NoContent,
                response
            )
        }
    }
}
