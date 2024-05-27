package com.xebia.functional.xef.server.models

import com.xebia.functional.xef.server.db.tables.Organization
import com.xebia.functional.xef.server.db.tables.Project
import com.xebia.functional.xef.server.db.tables.User
import com.xebia.functional.xef.server.db.tables.XefTokens
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(val authToken: String, val user: UserResponse)

@Serializable data class UserResponse(val id: Int, val name: String)

fun User.toUserResponse() = UserResponse(id.value, name)

@Serializable data class OrganizationSimpleResponse(val id: Int, val name: String)

fun Organization.toOrganizationSimpleResponse() = OrganizationSimpleResponse(id.value, name)

@Serializable
data class OrganizationFullResponse(val id: Int, val name: String, val owner: Int, val users: Long)

fun Organization.toOrganizationFullResponse() =
  OrganizationFullResponse(id.value, name, ownerId.value, users.count())

@Serializable data class ProjectSimpleResponse(val id: Int, val name: String, val orgId: Int)

fun Project.toProjectSimpleResponse() = ProjectSimpleResponse(id.value, name, orgId.value)

@Serializable
data class ProjectFullResponse(val id: Int, val name: String, val org: OrganizationFullResponse)

fun Project.toProjectFullResponse(org: Organization) =
  ProjectFullResponse(id.value, name, org.toOrganizationFullResponse())

@Serializable
data class TokenSimpleResponse(
  val id: Int,
  val projectId: Int,
  val userId: Int,
  val name: String,
  val token: String
)

fun XefTokens.toTokenSimpleResponse() =
  TokenSimpleResponse(id.value, projectId.value, userId.value, name, token)

@Serializable
data class TokenFullResponse(
  val id: Int,
  val project: ProjectSimpleResponse,
  val name: String,
  val token: String
)

fun XefTokens.toTokenFullResponse(project: ProjectSimpleResponse) =
  TokenFullResponse(id.value, project, name, token)
