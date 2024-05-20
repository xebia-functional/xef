package org.xef.xefMobile.model

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val authToken: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val authToken: String
)