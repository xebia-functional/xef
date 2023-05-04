package com.xebia.functional.kotlin

import java.util.Collections
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutorService
import scala.util.control.NoStackTrace
import scala.util.control.NonFatal

import cats.effect.*
import cats.effect.kernel.Async
import cats.effect.kernel.Resource.ExitCase
import cats.implicits.*

import arrow.fx.coroutines.Resource as ArrowResource

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.*

trait ArrowResourceToIO[F[_]] {
  def apply[A](r: ArrowResource[A]): Resource[F, A]
}

object ArrowResourceToIO {
  def apply[F[_]: Async]: ArrowResourceToIO[F] = new ArrowResourceToIO[F]:
    def apply[A](r: ArrowResource[A]): Resource[F, A] =
      val f: F[A] = CoroutineToIO[F]
        .runCancelable[
          kotlin.Pair[
            A,
            kotlin.jvm.functions.Function2[_ >: arrow.fx.coroutines.ExitCase, Continuation[kotlin.Unit], Any]
          ]
        ]((_, cont) => r.allocate(cont))
      Resource.makeCase(f) {
        case (pair, ExitCase.Canceled) => CoroutineToIO[F].runCancelable((_, cont) => pair.second.invoke(arrow.fx.coroutines.ExitCase.Canceled, cont))
        case (pair, ExitCase.Errored(e)) =>
          CoroutineToIO[F].runCancelable((_, cont) => pair.second.invoke(arrow.fx.coroutines.ExitCase.Error(e), cont))
        case (pair, ExitCase.Succeeded) =>
          CoroutineToIO[F].runCancelable((_, cont) => pair.second.invoke(arrow.fx.coroutines.ExitCase.Completed, cont))
      }
}
