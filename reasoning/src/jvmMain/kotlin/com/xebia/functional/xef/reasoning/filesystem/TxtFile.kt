package com.xebia.functional.xef.reasoning.filesystem

import kotlinx.serialization.Serializable

@Serializable data class TxtFile(val name: String, val content: String)
