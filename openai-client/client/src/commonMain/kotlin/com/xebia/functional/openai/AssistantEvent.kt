package com.xebia.functional.openai

import kotlinx.serialization.Serializable

// TODO write proper AssistantEvent
// According to:
// https://platform.openai.com/docs/api-reference/assistants-streaming/message-delta-object
@Serializable data class AssistantEvent(val todo: String)
