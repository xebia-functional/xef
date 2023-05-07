package com.xebia.functional.tools

import com.xebia.functional.Document

data class Tool(
    val name: String,
    val description: String,
    val action: suspend (prompt: String) -> List<Document>,
) {

}
