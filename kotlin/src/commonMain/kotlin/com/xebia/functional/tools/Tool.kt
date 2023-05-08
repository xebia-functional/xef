package com.xebia.functional.tools

import com.xebia.functional.Document
import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging

data class Tool(
    val name: String,
    val description: String,
    val action: suspend Tool.() -> List<Document>,
) {
    val logger: KLogger by lazy { KotlinLogging.logger(name) }
}
