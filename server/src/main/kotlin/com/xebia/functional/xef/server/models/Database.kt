package com.xebia.functional.xef.server.models

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
