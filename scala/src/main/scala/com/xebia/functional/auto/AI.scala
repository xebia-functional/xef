package com.xebia.functional.scala.auto

import cats.effect.kernel.Async
import cats.implicits.*
import com.xebia.functional.xef.prompt.PromptTemplate as KtPromptTemplate
import com.xebia.functional.xef.auto.SerializationConfig as KtSerializationConfig
import com.xebia.functional.xef.auto.AIScope as KtAIScope
import com.xebia.functional.xef.auto.AIException
import com.xebia.functional.xef.agents.*
import com.xebia.functional.xef.llm.openai.LLMModel as KtLLMModel
import com.xebia.functional.xef.auto.AIKt
import com.xebia.functional.loom.LoomAdapter
import com.xebia.functional.xef.AIError
import kotlin.coroutines.Continuation
import unwrapped.*

import scala.jdk.CollectionConverters.*

object AI:
  private type ContextualAgent = ParameterlessAgent[java.util.List[String]]

  def apply[A](scope: AIScope ?=> A): A =
    LoomAdapter.apply { cont =>
      AIKt.AIScope[A](
        (ktAiScope, _) => scope(using AIScope.fromCore(ktAiScope)),
        (e: AIError, _) => throw AIException(e.getReason),
        cont
      )
    }

  final case class AIScope private(kt: KtAIScope):
    def context[A](agent: ContextualAgent, scope: AIScope ?=> A): A =
      LoomAdapter.apply(kt.context[A](agent, (ktAiScope, _) => scope(using AIScope.fromCore(ktAiScope)), _))

    def context[A](agents: List[ContextualAgent], scope: AIScope ?=> A): A =
      LoomAdapter.apply(kt.context[A](agents.asJava, (ktAiScope, _) => scope(using AIScope.fromCore(ktAiScope)), _))

    def promptMessage[A](question: String, model: KtLLMModel): List[String] =
      LoomAdapter.apply[java.util.List[String]](kt.promptMessage(question, model, _)).asScala.toList

    def promptMessage[A](prompt: KtPromptTemplate[String], variables: Map[String, String], model: KtLLMModel): List[String] =
      LoomAdapter.apply[java.util.List[String]](kt.promptMessage(prompt, variables.asJava, model, _)).asScala.toList

  object AIScope:
    def fromCore(coreAIScope: KtAIScope): AIScope = new AIScope(coreAIScope)
