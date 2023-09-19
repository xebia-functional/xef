package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.xef.server.models.ProjectRequest
import com.xebia.functional.xef.server.models.ProjectUpdateRequest
import com.xebia.functional.xef.server.models.exceptions.XefExceptions
import com.xebia.functional.xef.server.services.ProjectRepositoryService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun Routing.projectsRoutes(
    projectRepositoryService: ProjectRepositoryService
) {
    authenticate("auth-bearer") {
        get("/v1/settings/projects") {
            val token = call.getToken()
            val response = projectRepositoryService.getProjects(token)
            call.respond(response)
        }
        get("/v1/settings/projects/{id}") {

            val token = call.getToken()
            val id = call.getProjectId()
            val response = projectRepositoryService.getProject(token, id)
            call.respond(response)
        }
        post("/v1/settings/projects") {

            val request = Json.decodeFromString<ProjectRequest>(call.receive<String>())
            val token = call.getToken()
            val response = projectRepositoryService.createProject(request, token)
            call.respond(
                status = HttpStatusCode.Created,
                response
            )
        }
        put("/v1/settings/projects/{id}") {
            val request = Json.decodeFromString<ProjectUpdateRequest>(call.receive<String>())
            val token = call.getToken()
            val id = call.getProjectId()
            val response = projectRepositoryService.updateProject(token, request, id)
            call.respond(
                status = HttpStatusCode.NoContent,
                response
            )
        }
        delete("/v1/settings/projects/{id}") {
            val token = call.getToken()
            val id = call.getProjectId()
            val response = projectRepositoryService.deleteProject(token, id)
            call.respond(
                status = HttpStatusCode.NoContent,
                response
            )
        }
    }
}

private fun ApplicationCall.getProjectId(): Int {
    return this.parameters["id"]?.toInt() ?: throw XefExceptions.ValidationException("Invalid id")
}

