package com.xef.xefMobile.model

import kotlinx.serialization.Serializable

@Serializable
data class Assistant(val id: String, val name: String)

@Serializable
data class AssistantsResponse(val data: List<Assistant>)
