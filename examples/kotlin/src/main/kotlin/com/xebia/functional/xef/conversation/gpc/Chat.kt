package com.xebia.functional.xef.conversation.gpc

import com.xebia.functional.xef.gcp.*
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  val gcp = GCP("xefdemo", VertexAIRegion.US_CENTRAL1)

  gcp.conversation {
    while (true) {
      print("\n🤖 Enter your question: ")
      val userInput = readlnOrNull() ?: break
      if (userInput == "exit") break
      val answer = promptMessage(Prompt(userInput), model = gcp.DEFAULT_CHAT)
      println("\n🤖 $answer")
    }
    println("\n🤖 Done")
  }
}
