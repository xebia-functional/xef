package com.xebia.functional.xef.auto.gpc

import com.xebia.functional.gpt4all.getOrThrow
import com.xebia.functional.xef.auto.conversation
import com.xebia.functional.xef.gcp.GcpChat

suspend fun main() {
  conversation {
    val gcp = autoClose(GcpChat("us-central1-aiplatform.googleapis.com", "xef-demo", "codechat-bison@001", "token"))
    while (true) {
      print("\n🤖 Enter your question: ")
      val userInput = readlnOrNull() ?: break
      val answer = gcp.promptMessage(userInput)
      println("\n🤖 $answer")
    }
    println("\n🤖 Done")
  }.getOrThrow()
}

