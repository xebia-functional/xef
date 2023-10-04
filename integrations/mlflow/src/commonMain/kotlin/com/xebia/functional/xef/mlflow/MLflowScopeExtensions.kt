package com.xebia.functional.xef.mlflow

import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.prompt.Prompt
import kotlinx.coroutines.flow.Flow

@AiDsl
suspend fun Conversation.promptMessage(
  prompt: Prompt,
  model: Chat = MLflow().DEFAULT_CHAT
): String = model.promptMessage(prompt, this)

@AiDsl
fun Conversation.promptStreaming(
  prompt: Prompt,
  model: Chat = MLflow().DEFAULT_CHAT
): Flow<String> = model.promptStreaming(prompt, this)
