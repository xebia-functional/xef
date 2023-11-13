package com.xebia.functional.xef.prompt

import com.xebia.functional.openai.models.ChatCompletionRole.*
import com.xebia.functional.xef.data.Question
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.assistantSteps
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PromptBuilderSpec :
  StringSpec({
    "buildPrompt should return the expected messages" {
      val messages =
        Prompt {
            +system("Test System")
            +user("Test Query")
            +assistant("Test Assistant")
          }
          .messages

      val messagesExpected =
        listOf(
          "Test System".message(system),
          "Test Query".message(user),
          "Test Assistant".message(assistant)
        )

      messages shouldBe messagesExpected
    }

    "buildPrompt should return the expected messages when using forEach" {
      val instructions = listOf("instruction 1", "instruction 2")

      val messages =
        Prompt {
            +system("Test System")
            +user("Test Query")
            instructions.forEach { +assistant(it) }
          }
          .messages

      val messagesExpected =
        listOf(
          "Test System".message(system),
          "Test Query".message(user),
          """
            |instruction 1
            |instruction 2
        """
            .trimMargin()
            .message(assistant),
        )

      messages shouldBe messagesExpected
    }

    "buildPrompt should return the expected messages when using steps with the number for every step" {
      val instructions = listOf("instruction 1", "instruction 2")

      val messages =
        Prompt {
            +system("Test System")
            +user("Test Query")
            +assistantSteps { instructions }
          }
          .messages

      val messagesExpected =
        listOf(
          "Test System".message(system),
          "Test Query".message(user),
          """
            |1 - instruction 1
            |2 - instruction 2
        """
            .trimMargin()
            .message(assistant),
        )

      messages shouldBe messagesExpected
    }

    "buildPrompt should return the expected messages when using serializable objects" {
      val question = Question("Test Question")

      val messages =
        Prompt {
            +system("Test System")
            +user(question)
          }
          .messages

      val messagesExpected =
        listOf(
          "Test System".message(system),
          question.message(user),
        )

      messages shouldBe messagesExpected
    }

    "Prompt should flatten the messages with the same role" {
      val messages =
        Prompt {
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
          "Test System".message(system),
          """
            |User message 1
            |User message 2
          """
            .trimMargin()
            .message(user),
          "Assistant message 1".message(assistant),
          "User message 3".message(user),
          """
                |Assistant message 2
                |Assistant message 3
            """
            .trimMargin()
            .message(assistant),
          "User message 4".message(user),
        )

      messages shouldBe messagesExpected
    }
  })
