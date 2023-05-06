package com.xebia.functional.tools

data class Tool(
    val name: String,
    val description: String,
    val action: suspend (prompt: String) -> String,
)
