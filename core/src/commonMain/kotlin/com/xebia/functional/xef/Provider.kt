package com.xebia.functional.xef

import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.llm.LLM
import kotlin.jvm.JvmField

interface Provider<Model : LLM> {
    val DEFAULT_CHAT: Chat
    val DEFAULT_EMBEDDING: Embeddings
    fun supportedModels(): List<Model>
}

fun <Model : LLM> Provider<Model>.modelById(modelId: String): Model? =
    supportedModels().find { it.name == modelId }