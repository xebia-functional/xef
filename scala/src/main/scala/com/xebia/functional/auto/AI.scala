package com.xebia.functional.auto

import com.xebia.functional.loom.LoomAdapter
import com.xebia.functional.xef.auto.AIScope as KtAIScope
import com.xebia.functional.xef.auto.AIException
import com.xebia.functional.xef.auto.AIKt
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.tokenizer.ModelType

//def example(using AIScope): String =
//    prompt[String]("What is your name?")

//val example: AIScope ?=> String =
//  prompt[String]("What is your name?")

object AI:

  def apply[A](model: ModelType = ModelType.GPT_3_5_TURBO, block: AIScope ?=> A): A =
    LoomAdapter.apply { (cont) =>
      AIKt.AIScope[A](
        { (coreAIScope, cont) =>
          given AIScope = AIScope.fromCore(coreAIScope)
          block
        },
        model,
        (e: AIError, cont) => throw AIException(e.getReason),
        cont
      )
    }

end AI

final case class AIScope(kt: KtAIScope):

  // TODO: Design signature for Scala3 w/ Json parser (with support for generating Json Schema)?
  def prompt[A](
      prompt: String,
      maxAttempts: Int = 5,
      llmMode: LLMModel = LLMModel.getGPT_3_5_TURBO
  ): A = ???

  def promptMessage(
      prompt: String,
      maxAttempts: Int = 5,
      llmMode: LLMModel = LLMModel.getGPT_3_5_TURBO
  ): String = ???

private object AIScope:
  def fromCore(coreAIScope: KtAIScope): AIScope = new AIScope(coreAIScope)
