package com.xebia.functional.xef.reasoning.text.choices

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.reasoning.internals.callModel

class Choose(
  private val model: ChatWithFunctions,
  private val scope: CoreAIScope,
) {
  suspend fun chooseBestOf(
    prompt: Prompt,
    choices: List<Choice>,
  ): Choice =
    callModel(
      model,
      scope,
      Prompt(
        """|
      |Given the following prompt:
      |```prompt
      |${prompt.message}
      |```
      |Choose the best of the following choices:
      |```choices
      |${choices.joinToString("\n")}
      |```
    """
          .trimMargin()
      )
    )
}
