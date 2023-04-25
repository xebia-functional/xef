package com.xebia.functional.callbacks

import cats.data.NonEmptySeq

trait BaseCallbackManager[F[_]] extends BaseCallbackHandler[F]:
  val isAsync: Boolean = false
  def addHandler(callback: BaseCallbackHandler[F]): F[Unit]
  def removeHandler(handler: BaseCallbackHandler[F]): F[Unit]
  def setHandlers(hanlders: NonEmptySeq[BaseCallbackHandler[F]]): F[Unit]
  def setHandler(handler: BaseCallbackHandler[F]): F[Unit] = setHandlers(NonEmptySeq.of(handler))
