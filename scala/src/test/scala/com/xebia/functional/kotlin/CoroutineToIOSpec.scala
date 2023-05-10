package com.xebia.functional.scala.kotlin

import scala.concurrent.duration.*
import cats.effect.*
import munit.CatsEffectSuite

class CoroutineToIOSpec extends CatsEffectSuite:

  // Sleeping via Kotlin's coroutines
  def kotlinSleep(duration: FiniteDuration): IO[Unit] =
    CoroutineToIO[IO].runCancelable_ { (_, cont) =>
      // Kotlin suspended function calls...
      kotlinx.coroutines.DelayKt.delay(duration.toMillis, cont)
    }

  test("KotlinCoroutines should convert Kotlin’s coroutines into cats.effect.IO") {

    val io = for
      _ <- IO.println("Running...")
      fiber <- kotlinSleep(10.seconds).start
      _ <- IO.sleep(1000.millis)
      _ <- fiber.cancel
      _ <- fiber.joinWithUnit
      str = "Done!"
      _ <- IO.println(str)
    yield str

    assertIO(io, "Done!")
  }
