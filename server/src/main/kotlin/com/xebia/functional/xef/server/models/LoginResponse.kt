package com.xebia.functional.xef.server.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(val authToken: String)
