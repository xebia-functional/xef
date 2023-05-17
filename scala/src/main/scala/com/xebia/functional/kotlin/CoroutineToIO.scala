package com.xebia.functional.scala.kotlin

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
import cats.implicits.*

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.*

trait CoroutineToIO[F[_]] {
  def runCancelable[A](block: kotlin.jvm.functions.Function2[CoroutineScope, Continuation[? >: A], ?]): F[A]

  def runCancelable_(block: kotlin.jvm.functions.Function2[CoroutineScope, Continuation[_ >: kotlin.Unit], Any]): F[Unit]
}

@SuppressWarnings(
  Array(
    "scalafix:DisableSyntax.null",
    "scalafix:DisableSyntax.throw",
    "scalafix:DisableSyntax.asInstanceOf"
  )
)
object CoroutineToIO:

  def apply[F[_]: Async]: CoroutineToIO[F] = new CoroutineToIO[F]:

    def runCancelable_(block: kotlin.jvm.functions.Function2[CoroutineScope, Continuation[_ >: kotlin.Unit], Any]): F[Unit] =
      runCancelable(block).void

    def runCancelable[A](
        block: kotlin.jvm.functions.Function2[CoroutineScope, Continuation[? >: A], ?]
    ): F[A] =
      coroutineToIOFactory[A](block, buildCancelToken)

    private def dispatcher: F[CoroutineDispatcher] =
      Async[F].executionContext.map { other =>
        Async[F].executionContext
        kotlinx.coroutines.ExecutorsKt.from(
          new AbstractExecutorService with ExecutionContextExecutorService {
            override def isShutdown = false
            override def isTerminated = false
            override def shutdown() = ()
            override def shutdownNow() = Collections.emptyList[Runnable]
            override def execute(runnable: Runnable): Unit = other.execute(runnable)
            override def reportFailure(t: Throwable): Unit = other.reportFailure(t)
            override def awaitTermination(length: Long, unit: TimeUnit): Boolean = false
          }
        )
      }

    private def coroutineToIOFactory[A](
        block: kotlin.jvm.functions.Function2[CoroutineScope, Continuation[? >: A], ?],
        buildCancelToken: (Deferred[_], DisposableHandle) => Option[F[Unit]]
    ): F[A] =
      dispatcher
        .flatMap { dispatcher =>
          Async[F].async[A] { cb =>
            Async[F].delay {
              try {
                val context = CoroutineContextKt.newCoroutineContext(
                  GlobalScope.INSTANCE,
                  dispatcher.asInstanceOf[CoroutineContext]
                )
                val deferred = kotlinx.coroutines.BuildersKt.async(
                  GlobalScope.INSTANCE,
                  context,
                  CoroutineStart.DEFAULT,
                  block
                )
                try {
                  val dispose = deferred.invokeOnCompletion { (e: Throwable) =>
                    e match {
                      case e: Throwable => cb(Left(e))
                      case null => cb(Right(deferred.getCompleted))
                    }
                    kotlin.Unit.INSTANCE
                  }
                  buildCancelToken(deferred, dispose)
                } catch {
                  case NonFatal(e) =>
                    deferred.cancel(null)
                    throw e
                }
              } catch {
                case NonFatal(e) =>
                  cb(Left(e))
                  None
              }
            }
          }
        }

    private def buildCancelToken(deferred: Deferred[_], dispose: DisposableHandle): Option[F[Unit]] =
      Some(Async[F].defer {
        deferred.cancel(PleaseCancel)
        dispose.dispose()
        coroutineToIOFactory[kotlin.Unit](
          (_, cont) => deferred.join(cont),
          (_, _) => None
        ).void
      })

    private object PleaseCancel extends CancellationException with NoStackTrace
