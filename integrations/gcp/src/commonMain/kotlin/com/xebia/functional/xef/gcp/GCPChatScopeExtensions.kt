package com.xebia.functional.xef.gcp

import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.prompt.Prompt
import kotlinx.coroutines.flow.Flow

@AiDsl
suspend fun Conversation.promptMessage(
  prompt: Prompt,
  model: Chat,
): String = model.promptMessage(prompt)

@AiDsl
suspend fun Conversation.promptMessage(
  prompt: Prompt,
  gcp: GCP,
): String = gcp.DEFAULT_CHAT.promptMessage(prompt)

@AiDsl
suspend fun Conversation.promptStreaming(
  prompt: Prompt,
  model: Chat,
): Flow<String> = model.promptStreaming(prompt, this)
