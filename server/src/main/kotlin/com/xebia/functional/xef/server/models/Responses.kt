package com.xebia.functional.xef.server.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(val authToken: String)

@Serializable
data class OrganizationSimpleResponse(val name: String)

@Serializable
data class OrganizationWithIdResponse(val id: Int, val name: String, val users: Long)
@Serializable
data class OrganizationFullResponse(val id: Int, val name: String, val owner: Int, val users: Long)

@Serializable
data class UserResponse(val id: Int, val name: String)
