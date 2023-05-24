package com.xebia.functional.auto

import com.xebia.functional.loom.LoomAdapter
import com.xebia.functional.scala.auto.ScalaSerialDescriptor
import com.xebia.functional.xef.auto.AIScope as KtAIScope
import com.xebia.functional.xef.auto.Agent as KtAgent
import com.xebia.functional.xef.auto.AIException
import com.xebia.functional.xef.auto.AIKt
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.llm.openai.LLMModel
import io.circe.{Decoder, Json}

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

  // TODO: Design signature for Scala3 w/ Json parser (with support for generating Json Schema)?
  def prompt[A](
    prompt: String,
    maxAttempts: Int = 5,
    llmMode: LLMModel = LLMModel.getGPT_3_5_TURBO,
    user: String = "testing",
    echo: Boolean = false,
    n: Int = 1,
    temperature: Double = 0.0,
    bringFromContext: Int = 10,
    minResponseTokens: Int = 500,
  )(using descriptor: ScalaSerialDescriptor[A])(using decoder: Decoder[A]): A =
    LoomAdapter.apply((cont) =>
      KtAgent.prompt[A](
        kt,
        prompt,
        descriptor,
        (a: String) => ???,
        maxAttempts,
        llmMode,
        user,
        echo,
        n,
        temperature,
        bringFromContext,
        minResponseTokens,
        cont
      )
    )


  def promptMessage(
                     prompt: String,
                     maxAttempts: Int = 5,
                     llmMode: LLMModel = LLMModel.getGPT_3_5_TURBO
                   ): String = ???

private object AIScope:
  def fromCore(coreAIScope: KtAIScope): AIScope = new AIScope(coreAIScope)
