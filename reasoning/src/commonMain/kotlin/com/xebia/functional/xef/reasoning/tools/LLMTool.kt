package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.internals.callModel
import com.xebia.functional.xef.tracing.Dispatcher
import com.xebia.functional.xef.tracing.ToolEvent
import com.xebia.functional.xef.tracing.createDispatcher
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

abstract class LLMTool(
  override val name: String,
  override val description: String,
  private val model: Chat,
  private val scope: Conversation,
  private val instructions: List<String> = emptyList(),
) : Tool {

  override suspend operator fun invoke(input: String): String {
    return callModel(
      model,
      scope,
      prompt =
        Prompt {
          +system("You are an expert in executing the tool:")
          +system("Tool: $name")
          +system("Description: $description")
          instructions.forEach { +system(it) }
          +user("input: $input")
          +assistant("output:")
        }
    ).also {
      scope.track(ToolEvent("🔧 $name[$input]"))
    }
  }

  companion object {
    @JvmStatic
    @JvmOverloads
    fun create(
      name: String,
      description: String,
      model: Chat,
      scope: Conversation,
      instructions: List<String> = emptyList(),
    ): LLMTool = object : LLMTool(name, description, model, scope, instructions) {}
  }
}
