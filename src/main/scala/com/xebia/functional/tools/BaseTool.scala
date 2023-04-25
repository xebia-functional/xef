package com.xebia.functional.tools

import cats.MonadThrow
import cats.data.NonEmptyMap
import cats.syntax.all.*
import eu.timepit.refined.*
import eu.timepit.refined.types.string.NonEmptyString
import com.xebia.functional.callbacks.BaseCallbackManager

abstract class BaseTool[F[_]: MonadThrow](
    name: NonEmptyString,
    description: NonEmptyString,
    // argsSchema: Option[???]
    returnDirect: Boolean = false,
    verbose: Boolean = false,
    callbackManager: BaseCallbackManager[F]
):
  def run(input: Map[String, String] | String): F[String]

  def _run(input: Map[String, String] | String): F[String] =
    run(input).onError(e => callbackManager.onToolError(e)).flatTap(ob => callbackManager.onToolEnd(ob))

  def call(
      toolInput: Map[String, String] | String,
      verbose: Option[Boolean] = None,
      startColor: Option[String] = Some("green"),
      color: Option[String] = Some("green")
  ): F[String] =
    val input0 = toolInput match
      case s: String => MonadThrow[F].pure(s)
      case m: Map[String, String] => MonadThrow[F].fromOption(m.headOption.map(_._2), new RuntimeException("Agent Input Map was empty"))

    for
      input <- input0
      _ <- callbackManager.onToolStart(Map("name" -> name.value, "description" -> description.value), input)
      observation <- run(toolInput)
    yield observation
