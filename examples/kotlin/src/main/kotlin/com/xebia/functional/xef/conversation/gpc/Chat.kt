package com.xebia.functional.xef.conversation.gpc

import com.xebia.functional.xef.gcp.*
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  val gcp = GCP("xefdemo", VertexAIRegion.US_CENTRAL1)

  // SUGGESTION 1: multiple uses of gcp instance
  gcp.conversation {
    while (true) {
      print("\n🤖 Enter your question: ")
      val userInput = readlnOrNull() ?: break
      if(userInput == "exit") break
      val answer = promptMessage(Prompt(userInput), model = gcp.DEFAULT_CHAT)
      println("\n🤖 $answer")
    }
    println("\n🤖 Done")
  }

  // SUGGESTION 2: one use of gcp when instantiating Conversation, gcp is stored as attribute of conversation
  gcp.conversation {
    while (true) {
      print("\n🤖 Enter your question: ")
      val userInput = readlnOrNull() ?: break
      if(userInput == "exit") break
      val answer = promptMessage2(Prompt(userInput))
      println("\n🤖 $answer")
    }
    println("\n🤖 Done")
  }
}
