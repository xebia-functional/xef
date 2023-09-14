package com.xebia.functional.xef.server.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(val authToken: String)

@Serializable
data class OrganizationResponse(val name: String, val message: String)
