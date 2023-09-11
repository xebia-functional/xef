package com.xebia.functional.xef.server.models

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Users(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("password_hash") val password: String,
    @SerialName("salt") val salt: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    @SerialName("auth_token") val authToken: String
)

@Serializable
data class Organization(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    @SerialName("owner_id") val ownerId: Int
)

@Serializable
data class Projects(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    @SerialName("org_id") val orgId: Int
)

@Serializable
data class UsersOrgRelation(
    @SerialName("user_id") val userId: Int,
    @SerialName("org_id") val orgId: Int
)

@Serializable
data class XefTokens(
    @SerialName("user_id") val userId: Int,
    @SerialName("project_id") val projectId: Int,
    @SerialName("name") val name: String,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("updated_at") val updatedAt: Instant,
    @SerialName("token") val token: String,
    @SerialName("providers_config") val providersConfig: ProvidersConfig
)
