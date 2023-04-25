package com.xebia.functional.callbacks

import cats.data.NonEmptySeq
import com.xebia.functional.llm.models.LLMResult
import com.xebia.functional.agents.models.{AgentAction, AgentFinish}

abstract class BaseCallbackHandler[F[_]](
    alwaysVerbose: Boolean = false,
    ignoreLlm: Boolean = false,
    ignoreChain: Boolean = false,
    ignoreAgent: Boolean = false
):
  def onLlmStart(
      serialized: Map[String, String],
      prompts: NonEmptySeq[String]
  ): F[Unit]

  def onLlmNewToken(token: String): F[Unit]

  def onLlmEnd(response: LLMResult): F[Unit]

  def onLlmError(error: Throwable): F[Unit]

  def onChainStart(serialized: Map[String, String], inputs: Map[String, String]): F[Unit]

  def onChainEnd(output: Map[String, String]): F[Unit]

  def onChainError(error: Throwable): F[Unit]

  def onToolStart(serialized: Map[String, String], inputValue: String): F[Unit]

  def onToolEnd(output: String): F[Unit]

  def onToolError(error: Throwable): F[Unit]

  def onText(text: String): F[Unit]

  def onAgentAction(action: AgentAction): F[Unit]

  def onAgentFinish(finish: AgentFinish): F[Unit]
