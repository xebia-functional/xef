package com.xebia.functional.xef.scala

import com.xebia.functional.loom.LoomAdapter
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.agents.LLMAgentKt
import com.xebia.functional.xef.llm.openai.LLMModel
import io.circe.parser.decode
import io.circe.{Decoder, Json}
import io.circe.parser.parse
import com.xebia.functional.xef.auto.{AIException, AIKt, AIScope as KtAIScope, Agent as KtAgent}

import scala.jdk.CollectionConverters.*

package object auto {

  def ai[A](block: AIScope ?=> A): A =
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

  def prompt[A: Decoder: ScalaSerialDescriptor](
      prompt: String,
      maxAttempts: Int = 5,
      llmModel: LLMModel = LLMModel.getGPT_3_5_TURBO,
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
        ScalaSerialDescriptor[A].serialDescriptor,
        (json: String) => parse(json).flatMap(Decoder[A].decodeJson(_)).fold(throw _, identity),
        maxAttempts,
        llmModel,
        user,
        echo,
        n,
        temperature,
        bringFromContext,
        minResponseTokens,
        cont
      )
    )

  def contextScope[A: Decoder: ScalaSerialDescriptor](docs: List[String])(block: AIScope ?=> A)(using scope: AIScope): A =
    LoomAdapter.apply(scope.kt.contextScopeWithDocs[A](docs.asJava, (_, _) => block, _))

  def promptMessage(
      prompt: String,
      llmModel: LLMModel = LLMModel.getGPT_3_5_TURBO,
      user: String = "testing",
      echo: Boolean = false,
      n: Int = 1,
      temperature: Double = 0.0,
      bringFromContext: Int = 10,
      minResponseTokens: Int = 500
  )(using scope: AIScope): List[String] =
    LoomAdapter
      .apply[java.util.List[String]](
        KtAgent.promptMessage(scope.kt, prompt, llmModel, user, echo, n, temperature, bringFromContext, minResponseTokens, _)
      ).asScala.toList

}
