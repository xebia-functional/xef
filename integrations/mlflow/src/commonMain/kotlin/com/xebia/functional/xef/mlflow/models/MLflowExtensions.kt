package com.xebia.functional.xef.mlflow.models

import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.usage.Usage
import com.xebia.functional.xef.mlflow.MlflowClient

fun Role.toMLflow(): MlflowClient.ChatRole {
  return when (this) {
    Role.SYSTEM -> MlflowClient.ChatRole.SYSTEM
    Role.USER -> MlflowClient.ChatRole.USER
    Role.ASSISTANT -> MlflowClient.ChatRole.ASSISTANT
  }
}

fun MlflowClient.ChatRole.fromMLflow(): Role {
  return when (this) {
    MlflowClient.ChatRole.SYSTEM -> Role.SYSTEM
    MlflowClient.ChatRole.USER -> Role.USER
    MlflowClient.ChatRole.ASSISTANT -> Role.ASSISTANT
  }
}

fun List<Message>.buildPrompt(): List<MlflowClient.ChatMessage> {
  return this.map { it -> MlflowClient.ChatMessage(it.role.toMLflow(), it.content) }
}

fun MlflowClient.ResponseMetadata.toUsage(): Usage {
  return Usage(this.inputTokens, this.outputTokens, this.totalTokens)
}

fun MlflowClient.ChatResponse.toChoices(): List<Choice> {
  return this.candidates.mapIndexed { idx, value ->
    Choice(
      Message(value.message.role.fromMLflow(), value.message.content, ""),
      value.metadata.finishReason,
      idx
    )
  }
}

fun MlflowClient.ChatResponse.toChunks(): List<ChatChunk> {
  return this.candidates.mapIndexed { idx, value ->
    ChatChunk(idx, ChatDelta(value.message.role.fromMLflow()), value.metadata.finishReason)
  }
}

fun MlflowClient.PromptResponse.toCompletionChoices(): List<CompletionChoice> {
  return this.candidates.mapIndexed { idx, value ->
    CompletionChoice(value.text, idx, finishReason = value.metadata?.finishReason)
  }
}

fun MlflowClient.EmbeddingsResponse.toEmbeddings(): List<Embedding> {
  return this.embeddings.map { Embedding(it) }
}
