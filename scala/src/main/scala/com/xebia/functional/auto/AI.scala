package com.xebia.functional.scala.auto

import cats.effect.kernel.Async
import cats.implicits.*
import com.xebia.functional.scala.kotlin.CoroutineToIO
import com.xebia.functional.auto.SerializationConfig as KtSerializationConfig
import com.xebia.functional.auto.AIScope as KtAIScope
import com.xebia.functional.tools.Agent as KtAgent
import com.xebia.functional.llm.openai.LLMModel as KtLLMModel
import com.xebia.functional.auto.AIKt
import unwrapped.*

object AI:

  def adapter[F[_]: Async, A](block: AIScope[F] ?=> A): F[A] =
    CoroutineToIO[F].runCancelable[KtAIScope] { (_, cont) =>
      val ktAIScope: KtAIScope = ???
      ktAIScope
    }.map(AIScope.fromCore[F])
      .map(block(using _))

  def ai[F[_] : Async, A](block: AIScope[F] ?=> A): F[A] =
    val f = adapter(block)
    CoroutineToIO[F].runCancelable[A] { (_, cont) =>
      AIKt.ai(???)
    }

  final case class AIScope[F[_] : Async] private(kt: KtAIScope):
    def agent[A](agent: KtAgent, scope: AIScope[F] ?=> A): F[A] = ???

    def agent[A](agents: List[KtAgent], scope: AIScope[F] ?=> A): F[A] = ???

    def prompt[A](
      prompt: String,
      serializationConfig: KtSerializationConfig[A],
      maxAttempts: Int = 5,
      llmMode: KtLLMModel = KtLLMModel("gpt-3.5-turbo", KtLLMModel.Kind.Chat)
    ): F[A] =
      CoroutineToIO[F].runCancelable[A] { (_, cont) => kt.prompt(prompt, serializationConfig, maxAttempts, llmMode, cont) }

  private object AIScope:
    def fromCore[F[_]: Async](coreAIScope: KtAIScope): AIScope[F] = new AIScope[F](coreAIScope)
