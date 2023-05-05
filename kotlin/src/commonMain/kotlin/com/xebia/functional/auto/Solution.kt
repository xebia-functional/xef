package com.xebia.functional.auto

import kotlinx.serialization.Serializable

@Serializable
data class Solution(
    val objective: String, val result: String, val accomplishesObjective: Boolean
)
