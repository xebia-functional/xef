package com.xebia.functional.xef.gcp

import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.prompt.Prompt
import kotlinx.coroutines.flow.Flow

@AiDsl
suspend fun Conversation.promptMessageGcp(
  prompt: Prompt,
  model: Chat = GCP().DEFAULT_CHAT
): String = model.promptMessage(prompt, this)

@AiDsl
fun Conversation.promptStreamingGcp(
  prompt: Prompt,
  model: Chat = GCP().DEFAULT_CHAT
): Flow<String> = model.promptStreaming(prompt, this)
