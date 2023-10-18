package com.xebia.functional.xef.server.models

import kotlinx.serialization.Serializable

@Serializable data class RegisterRequest(val name: String, val email: String, val password: String)

@Serializable data class LoginRequest(val email: String, val password: String)

@Serializable data class OrganizationRequest(val name: String)

@Serializable data class OrganizationUpdateRequest(val name: String, val owner: Int? = null)

@Serializable data class ProjectRequest(val name: String, val orgId: Int)

@Serializable data class ProjectUpdateRequest(val name: String, val orgId: Int? = null)

@Serializable data class TokenRequest(val name: String, val projectId: Int)

@Serializable data class TokenUpdateRequest(val name: String, val providerConfig: ProvidersConfig)
