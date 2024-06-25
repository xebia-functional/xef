package com.xebia.functional.xef.dsl.audio

import com.sipgate.mp3wav.Converter
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
import io.github.nomisrev.openapi.CreateSpeechRequest
import io.github.nomisrev.openapi.CreateTranscriptionRequest
import io.github.nomisrev.openapi.CreateTranslationRequest
import io.github.nomisrev.openapi.UploadFile
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.File
import javax.media.bean.playerbean.MediaPlayer

suspend fun main() {
  val config = Config()
  val audio = OpenAI(config).audio
  println("ask me something!")
  while (true) {
    val input = readlnOrNull() ?: break
    val modelResponse: String =
      AI(
        """
      Reply to the following prompt with one sentence of max 20 words appropriate for the context of the conversation.
      $input
      """
          .trimIndent()
      )
    val channel =
      audio.speech.createSpeech(
        CreateSpeechRequest(
          model = CreateSpeechRequest.Model.Tts1,
          input = modelResponse,
          voice = CreateSpeechRequest.Voice.Nova
        )
      )
    val wavConverter = Converter(channel.bodyAsChannel().toInputStream())
    val file = File("hello.wav")
    file.writeBytes(wavConverter.toByteArray())
    val mediaPlayer = MediaPlayer()
    mediaPlayer.mediaLocation = file.toURI().toURL().toString()
    // play the sound just once
    mediaPlayer.start()
    mediaPlayer.playbackLoop = false
    val uploadFile =
      UploadFile(
        filename = "hello.wav",
        contentType = null,
        size = null,
        bodyBuilder = { file.readBytes().forEach { writeByte(it) } }
      )
    val transcription =
      audio.transcriptions.createTranscription(
        CreateTranscriptionRequest(
          file = uploadFile,
          model = CreateTranscriptionRequest.Model.Whisper1,
          prompt = "Translate to spanish",
          language = "es"
        )
      )
    println("transcription in `es`:")
    println(transcription)
    val translation =
      audio.translations.createTranslation(
        CreateTranslationRequest(
          file = uploadFile,
          model = CreateTranslationRequest.Model.Whisper1,
          prompt = "Translate"
        )
      )
    println("translation to english:")
    println(translation)
    file.delete()
  }
}
