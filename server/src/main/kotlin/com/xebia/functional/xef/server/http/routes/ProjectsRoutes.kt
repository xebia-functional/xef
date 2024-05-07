package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.xef.server.models.ProjectFullResponse
import com.xebia.functional.xef.server.models.ProjectRequest
import com.xebia.functional.xef.server.models.ProjectSimpleResponse
import com.xebia.functional.xef.server.models.ProjectUpdateRequest
import com.xebia.functional.xef.server.services.ProjectRepositoryService
import guru.zoroark.tegral.openapi.dsl.schema
import guru.zoroark.tegral.openapi.ktor.resources.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import io.swagger.v3.oas.models.media.StringSchema

fun Routing.projectsRoutes(projectRepositoryService: ProjectRepositoryService) {
  authenticate("auth-bearer") {
    getD<ProjectsRoutes> {
      val token = call.getToken()
      val response = projectRepositoryService.getProjects(token)
      call.respond(response)
    }
    postD<ProjectsRoutes> {
      val request = call.decodeFromStringRequest<ProjectRequest>()
      val token = call.getToken()
      val response = projectRepositoryService.createProject(request, token)
      call.respond(status = HttpStatusCode.Created, response)
    }
    getD<ProjectDetailsRoutes> {
      val token = call.getToken()
      val id = call.getId()
      val response = projectRepositoryService.getProject(token, id)
      call.respond(response)
    }
    putD<ProjectDetailsRoutes> {
      val request = call.decodeFromStringRequest<ProjectUpdateRequest>()
      val token = call.getToken()
      val id = call.getId()
      val response = projectRepositoryService.updateProject(token, request, id)
      call.respond(status = HttpStatusCode.NoContent, response)
    }
    deleteD<ProjectDetailsRoutes> {
      val token = call.getToken()
      val id = call.getId()
      val response = projectRepositoryService.deleteProject(token, id)
      call.respond(status = HttpStatusCode.NoContent, response)
    }
    getD<ProjectsByOrganizationRoutes> {
      val token = call.getToken()
      val id = call.getId()
      val response = projectRepositoryService.getProjectsByOrganization(token, id)
      call.respond(response)
    }
  }
}

@Resource("/v1/settings/projects")
class ProjectsRoutes {
  companion object :
    ResourceDescription by describeResource({
      tags += "Projects"
      get {
        description = "Get all projects"
        HttpStatusCode.OK.value response
          {
            description = "The list of projects"
            json { schema<ProjectFullResponse>() }
          }
      }
      post {
        description = "Create a project"
        body {
          description = "The project to create"
          required = true
          json { schema<ProjectRequest>() }
        }
        HttpStatusCode.Created.value response
          {
            description = "The created project"
            json { schema<ProjectSimpleResponse>() }
          }
      }
    })
}

@Resource("/v1/settings/projects/{id}")
class ProjectDetailsRoutes {
  companion object :
    ResourceDescription by describeResource({
      tags += "Projects"
      "id" pathParameter
        {
          description = "Project ID"
          required = true
          schema = StringSchema()
          example = "project_ABC_123"
        }
      get {
        description = "Get a project"
        HttpStatusCode.OK.value response
          {
            description = "The project"
            json { schema<ProjectFullResponse>() }
          }
      }
      put {
        description = "Update a project"
        body {
          description = "The project to update"
          required = true
          json { schema<ProjectUpdateRequest>() }
        }
        HttpStatusCode.NoContent.value response
          {
            description = "The updated project"
            json { schema<ProjectFullResponse>() }
          }
      }
      delete {
        description = "Delete a project"
        HttpStatusCode.NoContent.value response { description = "The deleted project" }
      }
    })
}

@Resource("/v1/settings/projects/org/{id}")
class ProjectsByOrganizationRoutes {
  companion object :
    ResourceDescription by describeResource({
      tags += "Projects"
      "id" pathParameter
        {
          description = "Organization ID"
          required = true
          schema = StringSchema()
          example = "org_ABC_123"
        }
      get {
        description = "Get all projects by organization"
        tags += "Projects"
        HttpStatusCode.OK.value response
          {
            description = "The list of projects"
            json { schema<ProjectSimpleResponse>() }
          }
      }
    })
}
