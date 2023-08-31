package com.xebia.functional.xef.gcp

import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.prompt.Prompt

@AiDsl
suspend fun Conversation.promptMessage(
    prompt: Prompt,
    model: Chat,
): String {
    return model.promptMessage(prompt)
}

@AiDsl
suspend fun Conversation.promptMessage(
    prompt: Prompt,
    gcp: GCP,
): String {
    return gcp.DEFAULT_CHAT.promptMessage(prompt)
}

@AiDsl
suspend fun Conversation.promptMessage2( // this function can also be generic than specific to GCP
    prompt: Prompt,
    model: Chat = provider.DEFAULT_CHAT,
): String {
    return model.promptMessage(prompt, this)
}
