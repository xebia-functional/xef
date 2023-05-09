package com.xebia.functional.tool

import com.xebia.functional.textsplitters.TokenTextSplitter
import com.xebia.functional.tools.Tool

suspend fun search(vararg prompt: String): Array<out Tool> =
    prompt.map {
        bingSearch(
            search = it,
            TokenTextSplitter(modelName = "gpt-3.5-turbo", chunkSize = 100, chunkOverlap = 50)
        )
    }.toTypedArray()
