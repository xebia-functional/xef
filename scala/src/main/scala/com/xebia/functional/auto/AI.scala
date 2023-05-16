package com.xebia.functional.auto

import com.xebia.functional.loom.LoomAdapter
import com.xebia.functional.xef.auto.AIScope as KtAIScope
import com.xebia.functional.xef.auto.AIException
import com.xebia.functional.xef.auto.AIKt
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.agents.Agent as KtAgent
import com.xebia.functional.xef.agents.ParameterlessAgent
import com.xebia.functional.xef.llm.openai.LLMModel

//def example(using AIScope): String =
//    prompt[String]("What is your name?")

//val example: AIScope ?=> String =
//  prompt[String]("What is your name?")

object AI:

  def apply[A](block: AIScope ?=> A): A =
    LoomAdapter.apply { (cont) =>
      AIKt.AIScope[A](
        { (coreAIScope, cont) =>
          given AIScope = AIScope.fromCore(coreAIScope)
          block
        },
        (e: AIError, cont) => throw AIException(e.getReason),
        cont
      )
    }

end AI

final case class AIScope(kt: KtAIScope):
  def agent[A](agent: ParameterlessAgent[List[String]], scope: AIScope ?=> A): A = ???

  def agent[A](agents: List[ParameterlessAgent[List[String]]], scope: AIScope ?=> A): A = ???

  // TODO: Design signature for Scala3 w/ Json parser (with support for generating Json Schema)?
  def prompt[A](
      prompt: String,
      maxAttempts: Int = 5,
      llmMode: LLMModel = LLMModel.getGPT_3_5_TURBO
  ): A = ???

private object AIScope:
  def fromCore(coreAIScope: KtAIScope): AIScope = new AIScope(coreAIScope)
