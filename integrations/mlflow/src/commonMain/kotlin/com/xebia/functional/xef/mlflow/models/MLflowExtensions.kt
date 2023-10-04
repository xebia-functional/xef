package com.xebia.functional.xef.mlflow.models

import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.usage.Usage
import com.xebia.functional.xef.mlflow.MLflowClient

fun Role.toMLflow(): MLflowClient.ChatRole {
    return when (this) {
        Role.SYSTEM -> MLflowClient.ChatRole.SYSTEM
        Role.USER -> MLflowClient.ChatRole.USER
        Role.ASSISTANT -> MLflowClient.ChatRole.ASSISTANT
    }
}

fun MLflowClient.ChatRole.fromMLflow(): Role {
    return when (this) {
        MLflowClient.ChatRole.SYSTEM -> Role.SYSTEM
        MLflowClient.ChatRole.USER -> Role.USER
        MLflowClient.ChatRole.ASSISTANT -> Role.ASSISTANT
    }
}

fun List<Message>.buildPrompt(): List<MLflowClient.ChatMessage> {
    return this.map { it ->
        MLflowClient.ChatMessage(it.role.toMLflow(), it.content)
    }
}

fun MLflowClient.ResponseMetadata.toUsage(): Usage {
    return Usage(this.inputTokens, this.outputTokens, this.totalTokens)
}

fun MLflowClient.ChatResponse.toChoices(): List<Choice> {
    return this.candidates.mapIndexed { idx, value ->
        Choice(Message(value.message.role.fromMLflow(), value.message.content, ""), value.metadata.finishReason, idx)
    }
}

fun MLflowClient.ChatResponse.toChunks(): List<ChatChunk> {
    return this.candidates.mapIndexed { idx, value ->
        ChatChunk(idx, ChatDelta(value.message.role.fromMLflow()), value.metadata.finishReason)
    }
}

fun MLflowClient.PromptResponse.toCompletionChoices(): List<CompletionChoice> {
    return this.candidates.mapIndexed { idx, value ->
        CompletionChoice(value.text, idx, finishReason = value.metadata?.finishReason)
    }
}

fun MLflowClient.EmbeddingsResponse.toEmbeddings(): List<Embedding> {
    return this.embeddings.map { Embedding(it) }
}
