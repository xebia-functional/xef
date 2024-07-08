package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.openapi.CreateChatCompletionRequest
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.assistant
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.system
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.user
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PromptBuilderSpec :
  StringSpec({
    val model = CreateChatCompletionRequest.Model.Gpt4
    "buildPrompt should return the expected messages" {
      val messages =
        Prompt(model) {
            +system("Test System")
            +user("Test Query")
            +assistant("Test Assistant")
          }
          .messages

      val messagesExpected =
        listOf(system("Test System"), user("Test Query"), assistant("Test Assistant"))

      messages shouldBe messagesExpected
    }

    "buildPrompt should return the expected messages when using forEach" {
      val instructions = listOf("instruction 1", "instruction 2")

      val messages =
        Prompt(model) {
            +system("Test System")
            +user("Test Query")
            instructions.forEach { +assistant(it) }
          }
          .messages

      val messagesExpected =
        listOf(
          system("Test System"),
          user("Test Query"),
          assistant(
            """
            |instruction 1
            |instruction 2
          """
              .trimMargin()
          )
        )

      messages shouldBe messagesExpected
    }

    "Prompt should flatten the messages with the same role" {
      val messages =
        Prompt(model) {
            +system("Test System")
            +user("User message 1")
            +user("User message 2")
            +assistant("Assistant message 1")
            +user("User message 3")
            +assistant("Assistant message 2")
            +assistant("Assistant message 3")
            +user("User message 4")
          }
          .messages

      val messagesExpected =
        listOf(
          system("Test System"),
          user(
            """
            |User message 1
            |User message 2
          """
              .trimMargin()
          ),
          assistant("Assistant message 1"),
          user("User message 3"),
          assistant(
            """
            |Assistant message 2
            |Assistant message 3
          """
              .trimMargin()
          ),
          user("User message 4")
        )

      messages shouldBe messagesExpected
    }
  })
