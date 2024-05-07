package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.xef.server.models.OrganizationFullResponse
import com.xebia.functional.xef.server.models.OrganizationRequest
import com.xebia.functional.xef.server.models.OrganizationUpdateRequest
import com.xebia.functional.xef.server.models.UserResponse
import com.xebia.functional.xef.server.services.OrganizationRepositoryService
import guru.zoroark.tegral.openapi.dsl.schema
import guru.zoroark.tegral.openapi.ktor.resources.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import io.swagger.v3.oas.models.media.StringSchema

fun Routing.organizationRoutes(orgRepositoryService: OrganizationRepositoryService) {
  authenticate("auth-bearer") {
    getD<OrganizationRoutes> {
      val token = call.getToken()
      val response = orgRepositoryService.getOrganizations(token)
      call.respond(response)
    }
    postD<OrganizationRoutes> {
      val request = call.decodeFromStringRequest<OrganizationRequest>()
      val token = call.getToken()
      val response = orgRepositoryService.createOrganization(request, token)
      call.respond(status = HttpStatusCode.Created, response)
    }
    getD<OrganizationDetailsRoutes> {
      val token = call.getToken()
      val id = call.getId()
      val response = orgRepositoryService.getOrganization(token, id)
      call.respond(response)
    }
    putD<OrganizationDetailsRoutes> {
      val request = call.decodeFromStringRequest<OrganizationUpdateRequest>()
      val token = call.getToken()
      val id = call.getId()
      val response = orgRepositoryService.updateOrganization(token, request, id)
      call.respond(status = HttpStatusCode.NoContent, response)
    }
    deleteD<OrganizationDetailsRoutes> {
      val token = call.getToken()
      val id = call.getId()
      val response = orgRepositoryService.deleteOrganization(token, id)
      call.respond(status = HttpStatusCode.NoContent, response)
    }
    getD<OrganizationUsersRoutes> {
      val token = call.getToken()
      val id = call.getId()
      val response = orgRepositoryService.getUsersInOrganization(token, id)
      call.respond(response)
    }
  }
}

@Resource("/v1/settings/org")
class OrganizationRoutes {
  companion object :
    ResourceDescription by describeResource({
      get {
        description = "Get all organizations"
        tags += "Organization"
        HttpStatusCode.OK.value response
          {
            description = "List of organizations"
            json { schema<List<OrganizationFullResponse>>() }
          }
      }
      post {
        description = "Create an organization"
        tags += "Organization"
        body {
          description = "The organization to create"
          required = true
          json { schema<OrganizationRequest>() }
        }
      }
    })
}

@Resource("/v1/settings/org/{id}")
class OrganizationDetailsRoutes {
  companion object :
    ResourceDescription by describeResource({
      "id" pathParameter
        {
          description = "Organization ID"
          required = true
          schema = StringSchema()
          example = "org_123"
        }
      get {
        description = "Get organization details"
        tags += "Organization"
        HttpStatusCode.OK.value response
          {
            description = "Organization details"
            json { schema<OrganizationFullResponse>() }
          }
      }
      put {
        description = "Update organization details"
        tags += "Organization"
        body {
          description = "The organization details to update"
          required = true
          json { schema<OrganizationUpdateRequest>() }
        }
        HttpStatusCode.NoContent.value response { description = "Organization updated" }
      }
      delete {
        description = "Delete organization"
        tags += "Organization"
      }
    })
}

@Resource("/v1/settings/org/{id}/users")
class OrganizationUsersRoutes {
  companion object :
    ResourceDescription by describeResource({
      "id" pathParameter
        {
          description = "Organization ID"
          required = true
          schema = StringSchema()
          example = "org_123"
        }
      get {
        description = "Get users in organization"
        tags += "Organization"
        HttpStatusCode.OK.value response
          {
            description = "List of users in organization"
            json { schema<List<UserResponse>>() }
          }
      }
    })
}
