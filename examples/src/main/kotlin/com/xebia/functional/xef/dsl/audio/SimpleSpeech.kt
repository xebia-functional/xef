package com.xebia.functional.xef.dsl.audio

import com.sipgate.mp3wav.Converter
import com.xebia.functional.openai.UploadFile
import com.xebia.functional.openai.generated.model.CreateSpeechRequest
import com.xebia.functional.openai.generated.model.CreateSpeechRequestModel
import com.xebia.functional.openai.generated.model.CreateTranscriptionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.OpenAI
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
      audio.createSpeech(
        CreateSpeechRequest(
          model = CreateSpeechRequestModel._1,
          input = modelResponse,
          voice = CreateSpeechRequest.Voice.nova
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
      audio.createTranscription(
        model = CreateTranscriptionRequestModel.whisper_1,
        prompt = "Translate to spanish",
        file = uploadFile,
        language = "es",
      )
    println("transcription in `es`:")
    println(transcription)
    val translation =
      audio.createTranslation(
        model = CreateTranscriptionRequestModel.whisper_1,
        prompt = "Translate",
        file = uploadFile,
      )
    println("translation to english:")
    println(translation)
    file.delete()
  }
}
