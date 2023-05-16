package com.xebia.functional.auto

import com.xebia.functional.loom.LoomAdapter
import com.xebia.functional.xef.auto.AIScope as KtAIScope
import com.xebia.functional.xef.auto.AIKt
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.agents.Agent as KtAgent
import com.xebia.functional.xef.agents.*
import com.xebia.functional.xef.llm.openai.LLMModel

def example(): AIScope ?=> String =
    prompt("What is your name?")

object AI:

  def apply[A](block: AIScope ?=> A): A =
    LoomAdapter.apply { (cont) =>
      AIKt.AIScope({ ktAIScope =>
        given AIScope.fromCore(ktAIScope)
        block()
      }, { e => throw AIException(e.reason) }, cont)
    }

end AI

final case class AIScope private(kt: KtAIScope):
  def agent[A](agent: ParameterlessAgent[List[String]], scope: AIScope ?=> A): A = ???

  def agent[A](agents: List[ParameterlessAgent[List[String]]], scope: AIScope ?=> A): A = ???

  // Scala3 Json parser with support for generating Json Schema?
  def prompt[A](
                 prompt: String,
                 maxAttempts: Int = 5,
                 llmMode: LLMModel = LLMModel("gpt-3.5-turbo", LLMModel.Kind.Chat)
               ): A = ???

private object AIScope:
  def fromCore(coreAIScope: KtAIScope): AIScope = new AIScope(coreAIScope)
