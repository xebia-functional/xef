package com.xebia.functional.xef.llm

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.image
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.user
import io.github.nomisrev.openapi.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.serializer

suspend inline fun <reified A> Chat.visionStructured(
  prompt: String,
  url: String,
  conversation: Conversation = Conversation(),
  model: CreateChatCompletionRequest.Model = CreateChatCompletionRequest.Model.Gpt4VisionPreview,
  functionsModel: CreateChatCompletionRequest.Model =
    CreateChatCompletionRequest.Model.Gpt35Turbo0125
): A {
  val response = vision(prompt, url, model, conversation).toList().joinToString("") { it }
  return prompt(Prompt(functionsModel) { +user(response) }, conversation, serializer())
}

fun Chat.vision(
  prompt: String,
  url: String,
  model: CreateChatCompletionRequest.Model = CreateChatCompletionRequest.Model.Gpt4VisionPreview,
  conversation: Conversation = Conversation()
): Flow<String> =
  promptStreaming(prompt = Prompt(model) { +image(url, prompt) }, scope = conversation)

suspend fun Image.asInputProvider(): UploadFile {
  val url = url
  val b64Json = b64Json
  when {
    url != null -> {
      val source = HttpClient().prepareGet(url).execute().body<ByteReadPacket>()
      return UploadFile(filename = "image", bodyBuilder = { source.copyTo(this) })
    }
    b64Json != null -> {
      return UploadFile(filename = "image", bodyBuilder = { writeText(b64Json) })
    }
    else -> error("Image has no content")
  }
}
