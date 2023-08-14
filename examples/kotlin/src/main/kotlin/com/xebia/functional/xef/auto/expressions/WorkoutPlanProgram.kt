package com.xebia.functional.xef.auto.expressions

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.buildPrompt
import com.xebia.functional.xef.prompt.expressions.Expression
import com.xebia.functional.xef.prompt.expressions.ExpressionResult
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.serpapi.Search
import com.xebia.functional.xef.reasoning.tools.LLMTool
import com.xebia.functional.xef.reasoning.tools.Tool

suspend fun taskSplitter(
  scope: Conversation,
  model: ChatWithFunctions,
  prompt: String,
  tools: List<Tool>
): ExpressionResult =
  Expression.run(
    scope = scope,
    model = model,
    block = {
      addMessages(
        buildPrompt {
          +system("You are a professional task planner")
          +user(
            """
     |I want to achieve:
  """
              .trimMargin()
          )
          +user(prompt)
          +assistant("I have access to all these tool")
          tools.forEach { +assistant("${it.name}: ${it.description}") }
          +assistant(
            """
     |I will break down your task into 3 tasks to make progress and help you accomplish this goal
     |using the tools that I have available.
     |1: ${prompt("task1")}
     |2: ${prompt("task2")}
     |3: ${prompt("task3")}
  """
              .trimMargin()
          )
        }
      )
    }
  )

suspend fun main() {

  OpenAI.conversation {
    val model = OpenAI().DEFAULT_SERIALIZATION
    val math =
      LLMTool.create(
        name = "Calculator",
        description =
          "Perform math operations and calculations processing them with an LLM model. The tool input is a simple string containing the operation to solve expressed in numbers and math symbols.",
        model = model,
        scope = this
      )
    val search = Search(model = model, scope = this)
    val plan =
      taskSplitter(
        scope = this,
        model = model,
        prompt =
          "Find and multiply the number of Leonardo di Caprio's girlfriends by the number of Metallica albums",
        tools = listOf(search, math)
      )
    println("--------------------")
    println("Plan")
    println("--------------------")
    println("🤖 replaced: ${plan.values.replacements.joinToString { it.key }}")
    println("--------------------")
    println(plan.result)
    println("--------------------")
  }
}
