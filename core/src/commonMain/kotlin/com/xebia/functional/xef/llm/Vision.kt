package com.xebia.functional.xef.llm

import com.xebia.functional.openai.UploadFile
import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.openai.generated.model.Image
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.image
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.user
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
  model: CreateChatCompletionRequestModel = CreateChatCompletionRequestModel.gpt_4_vision_preview,
  functionsModel: CreateChatCompletionRequestModel =
    CreateChatCompletionRequestModel.gpt_3_5_turbo_0125
): A {
  val response = vision(prompt, url, model, conversation).toList().joinToString("") { it }
  return prompt(Prompt(functionsModel) { +user(response) }, conversation, serializer())
}

fun Chat.vision(
  prompt: String,
  url: String,
  model: CreateChatCompletionRequestModel = CreateChatCompletionRequestModel.gpt_4_vision_preview,
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
