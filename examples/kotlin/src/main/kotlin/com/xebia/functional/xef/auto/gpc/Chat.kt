package com.xebia.functional.xef.auto.gpc

import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.gcp.GcpChat

suspend fun main() {
  OpenAI.conversation {
    val gcp =
      autoClose(
        GcpChat("us-central1-aiplatform.googleapis.com", "xef-demo", "codechat-bison@001", "token")
      )
    while (true) {
      print("\n🤖 Enter your question: ")
      val userInput = readlnOrNull() ?: break
      val answer = gcp.promptMessage(userInput)
      println("\n🤖 $answer")
    }
    println("\n🤖 Done")
  }
}
