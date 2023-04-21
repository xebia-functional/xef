package com.xebia.functional.loaders

import com.xebia.functional.domain.Document
import com.xebia.functional.textsplitters.BaseTextSplitter

interface BaseLoader {
    suspend fun load(): List<Document>
    suspend fun loadAndSplit(textSplitter: BaseTextSplitter): List<Document>
}
