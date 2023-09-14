package com.xebia.functional.xef.server.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class OrganizationRequest(
    val name: String
)

@Serializable
data class OrganizationUpdateRequest(
    val id: Int,
    val name: String,
    val owner: Int? = null
)
