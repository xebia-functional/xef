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
import io.circe.parser.parse

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

final case class AIScope(kt: KtAIScope)
private object AIScope:
  def fromCore(coreAIScope: KtAIScope): AIScope = new AIScope(coreAIScope)

def prompt[A: Decoder: SerialDescriptor](
    prompt: String,
    maxAttempts: Int = 5,
    llmMode: LLMModel = LLMModel.getGPT_3_5_TURBO,
    user: String = "testing",
    echo: Boolean = false,
    n: Int = 1,
    temperature: Double = 0.0,
    bringFromContext: Int = 10,
    minResponseTokens: Int = 500
)(using scope: AIScope): A =
  LoomAdapter.apply((cont) =>
    KtAgent.promptWithSerializer[A](
      scope.kt,
      prompt,
      SerialDescriptor[A].serialDescriptor,
      decode[A](_).fold(throw _, identity),
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
