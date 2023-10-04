package com.xebia.functional.xef.conversation.mlflow

import com.xebia.functional.xef.mlflow.MLflow
import com.xebia.functional.xef.mlflow.promptMessage
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  MLflow.conversation {
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
