package com.xebia.functional.xef.gcp

import arrow.core.nonEmptyListOf
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.prompt.Prompt

@AiDsl
suspend fun Conversation.promptMessage(
    prompt: Prompt
): String {
    val token =
        getenv("GCP_TOKEN") ?: throw AIError.Env.GCP(nonEmptyListOf("missing GCP_TOKEN env var"))

    val model =
        GcpChat("codechat-bison@001", GcpConfig(token, "xefdemo", "us-central1"))
    return model.promptMessage(prompt, this)
}
