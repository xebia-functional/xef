package com.xebia.functional.xef.conversation.gpc

import com.xebia.functional.xef.gcp.GCP
import com.xebia.functional.xef.gcp.VertexAIRegion
import com.xebia.functional.xef.gcp.conversation
import com.xebia.functional.xef.gcp.promptMessageGcp
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  val gcp = GCP("xefdemo", VertexAIRegion.US_CENTRAL1)
  gcp.conversation {}

  GCP.conversation {
    while (true) {
      print("\n🤖 Enter your question: ")
      val userInput = readlnOrNull() ?: break
      if (userInput == "exit") break
      val answer = promptMessageGcp(Prompt(userInput))
      println("\n🤖 $answer")
    }
    println("\n🤖 Done")
  }
}
