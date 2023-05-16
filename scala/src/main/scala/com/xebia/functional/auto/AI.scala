package com.xebia.functional.scala.auto

import cats.effect.kernel.Async
import cats.implicits.*
import com.xebia.functional.scala.kotlin.CoroutineToIO
import com.xebia.functional.xef.prompt.PromptTemplate as KtPromptTemplate
import com.xebia.functional.xef.auto.SerializationConfig as KtSerializationConfig
import com.xebia.functional.xef.auto.AIScope as KtAIScope
import com.xebia.functional.xef.agents.ParameterlessAgent
import com.xebia.functional.xef.llm.openai.LLMModel as KtLLMModel
import com.xebia.functional.xef.auto.AIKt
import unwrapped.*

import scala.jdk.CollectionConverters.*

object AI:
  private type ContextualAgent = ParameterlessAgent[java.util.List[String]]

  private def adapter[F[_]: Async, A](block: AIScope[F] ?=> A): F[A] =
    CoroutineToIO[F]
      .runCancelable[KtAIScope] { (_, cont) =>
        val ktAIScope: KtAIScope = ???
        ktAIScope
      }.map(AIScope.fromCore[F])
      .map(block(using _))

  def ai[F[_]: Async, A](block: AIScope[F] ?=> A): F[A] =
    val f = adapter(block)
    CoroutineToIO[F].runCancelable[A] { (_, cont) =>
      AIKt.ai(???)
    }

  final case class AIScope[F[_]: Async] private (kt: KtAIScope):
    def context[A](agent: ContextualAgent, scope: AIScope[F] ?=> A): F[A] = ???

    def context[A](agents: List[ContextualAgent], scope: AIScope[F] ?=> A): F[A] = ???

    def promptMessage[A](question: String, model: KtLLMModel): F[List[String]] =
      CoroutineToIO[F]
        .runCancelable[java.util.List[String]]((_, cont) => kt.promptMessage(question, model, cont))
        .map(_.asScala.toList)

    def promptMessage[A](prompt: KtPromptTemplate[String], variables: Map[String, String], model: KtLLMModel): F[List[String]] =
      CoroutineToIO[F]
        .runCancelable[java.util.List[String]]((_, cont) => kt.promptMessage(prompt, variables.asJava, model, cont))
        .map(_.asScala.toList)

  private object AIScope:
    def fromCore[F[_]: Async](coreAIScope: KtAIScope): AIScope[F] = new AIScope[F](coreAIScope)
