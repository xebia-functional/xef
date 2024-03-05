package com.xebia.functional.xef.dsl.audio

import com.sipgate.mp3wav.Converter
import com.xebia.functional.openai.apis.UploadFile
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.Audio
import io.ktor.utils.io.jvm.javaio.*
import java.io.File
import javax.media.bean.playerbean.MediaPlayer

suspend fun main() {
  val audio = Audio()
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
    val channel = audio.speech(modelResponse)
    val wavConverter = Converter(channel.toInputStream())
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
      audio.transcription(
        prompt = "Translate to spanish",
        file = uploadFile,
        language = "es",
      )
    println("transcription in `es`:")
    println(transcription)
    val translation =
      audio.translation(
        prompt = "Translate",
        file = uploadFile,
      )
    println("translation to english:")
    println(translation)
    file.delete()
  }
}
