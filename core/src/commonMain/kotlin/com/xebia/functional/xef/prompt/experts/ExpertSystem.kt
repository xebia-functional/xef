package com.xebia.functional.xef.prompt.experts

import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder
import com.xebia.functional.xef.prompt.templates.steps
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

object ExpertSystem {
  @JvmName("prompt")
  @JvmStatic
  operator fun invoke(system: String, query: String, instructions: List<String>): Prompt =
    PromptBuilder()
      .apply {
        +system
        +query
        +steps { instructions.forEach { +it } }
      }
      .build()
}
