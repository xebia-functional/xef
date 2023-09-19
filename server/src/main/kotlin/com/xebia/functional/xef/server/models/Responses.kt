package com.xebia.functional.xef.server.models

import com.xebia.functional.xef.server.db.tables.Organization
import com.xebia.functional.xef.server.db.tables.Project
import com.xebia.functional.xef.server.db.tables.User
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(val authToken: String)

@Serializable
data class UserResponse(val id: Int, val name: String)

fun User.toUserResponse() = UserResponse(id.value, name)

@Serializable
data class OrganizationSimpleResponse(val name: String)

fun Organization.toOrganizationSimpleResponse() = OrganizationSimpleResponse(name)

@Serializable
data class OrganizationWithIdResponse(val id: Int, val name: String, val users: Long)

fun Organization.toOrganizationWithIdResponse() = OrganizationWithIdResponse(id.value, name, users.count())

@Serializable
data class OrganizationFullResponse(val id: Int, val name: String, val owner: Int, val users: Long)

fun Organization.toOrganizationFullResponse() = OrganizationFullResponse(id.value, name, ownerId.value, users.count())

@Serializable
data class ProjectSimpleResponse(val name: String, val orgId: Int)

fun Project.toProjectSimpleResponse() = ProjectSimpleResponse(name, orgId.value)

@Serializable
data class ProjectWithIdResponse(val id: Int, val name: String, val org: OrganizationWithIdResponse)

fun Project.toProjectWithIdResponse(org: Organization) = ProjectWithIdResponse(id.value, name, org.toOrganizationWithIdResponse())
