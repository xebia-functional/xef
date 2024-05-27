package com.xef.xefMobile.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class RegisterResponse(

    val authToken: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val authToken: String,
    val user: UserResponse
)

@Serializable
data class UserResponse(
    val id: Int,
    val name: String
)