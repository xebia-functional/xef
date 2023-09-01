package com.xebia.functional.xef.conversation.gpc

import com.xebia.functional.xef.gcp.GCP
import com.xebia.functional.xef.gcp.promptMessage
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  GCP.conversation {
    while (true) {
      print("\n🤖 Enter your question: ")
      val userInput = readlnOrNull() ?: break
      if (userInput == "exit") break
      val answer = promptMessage(Prompt(userInput))
      println("\n🤖 $answer")
    }
    println("\n🤖 Done")
  }
}
