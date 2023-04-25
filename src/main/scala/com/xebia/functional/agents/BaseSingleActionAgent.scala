package com.xebia.functional.agents

import cats.MonadThrow
import cats.syntax.all.*
import cats.data.NonEmptySeq
import eu.timepit.refined.types.string.NonEmptyString
import com.xebia.functional.agents.models.*
import com.xebia.functional.llm.LLM
import com.xebia.functional.tools.BaseTool
import com.xebia.functional.callbacks.BaseCallbackManager
import java.nio.file.Path

abstract class BaseSingleActionAgent[F[_]: MonadThrow](
    inputKeys: NonEmptySeq[NonEmptyString]
):
  def getAllowedTools: Option[List[String]] = None
  def returnStoppedResponse(
      earlyStoppingMethod: String,
      intermediateSteps: List[LLMAgentHistory]
  ): F[AgentFinish] =
    if earlyStoppingMethod == "force"
    then MonadThrow[F].pure(AgentFinish(Map("output" -> "Agent stopped due to iteration limit or time limit."), ""))
    else MonadThrow[F].raiseError(UnsupportedEarlyStoppingMethodError(earlyStoppingMethod))

  def plan(intermediateSteps: List[LLMAgentHistory]): AgentAction | AgentFinish

  def fromLLMAndTools(
      llm: LLM[F],
      tools: NonEmptySeq[BaseTool[F]],
      callbackManager: Option[BaseCallbackManager[F]] = None
  ): F[BaseSingleActionAgent[F]]
