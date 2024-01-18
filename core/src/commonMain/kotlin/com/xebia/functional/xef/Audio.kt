package com.xebia.functional.xef

import ai.xef.openai.OpenAIModel
import ai.xef.openai.StandardModel
import com.xebia.functional.openai.apis.AudioApi
import com.xebia.functional.openai.apis.UploadFile
import com.xebia.functional.openai.models.CreateSpeechRequest
import com.xebia.functional.openai.models.CreateSpeechRequestModel
import com.xebia.functional.openai.models.CreateTranscriptionRequestModel
import com.xebia.functional.xef.llm.fromEnvironment
import io.ktor.client.statement.*
import io.ktor.utils.io.*

data class Audio(val api: AudioApi = fromEnvironment(::AudioApi)) {

  suspend fun speech(
    input: String,
    model: OpenAIModel<CreateSpeechRequestModel> = StandardModel(CreateSpeechRequestModel.tts_1),
    voice: CreateSpeechRequest.Voice = CreateSpeechRequest.Voice.alloy,
    responseFormat: CreateSpeechRequest.ResponseFormat = CreateSpeechRequest.ResponseFormat.mp3,
    speed: Double = 1.0
  ): ByteReadChannel {
    val response =
      api.createSpeech(
        CreateSpeechRequest(
          model = model,
          input = input,
          voice = voice,
          responseFormat = responseFormat,
          speed = speed,
        )
      )
    return response.response.bodyAsChannel()
  }

  suspend fun transcription(
    file: UploadFile,
    prompt: String,
    model: CreateTranscriptionRequestModel = CreateTranscriptionRequestModel.whisper_1,
    responseFormat: AudioApi.ResponseFormatCreateTranscription =
      AudioApi.ResponseFormatCreateTranscription.json,
    temperature: Double = 0.0,
    language: String? = null,
  ): String =
    api
      .createTranscription(
        file = file,
        model = model,
        language = language,
        prompt = prompt,
        responseFormat = responseFormat,
        temperature = temperature,
      )
      .body()
      .text

  suspend fun translation(
    file: UploadFile,
    prompt: String,
    model: CreateTranscriptionRequestModel = CreateTranscriptionRequestModel.whisper_1,
    responseFormat: String? = "json",
    temperature: Double = 0.0
  ): String =
    api
      .createTranslation(
        file = file,
        model = model,
        prompt = prompt,
        responseFormat = responseFormat,
        temperature = temperature,
      )
      .body()
      .text
}
