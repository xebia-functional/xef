package com.xebia.functional.scala.auto

import cats.syntax.either.*
import munit.FunSuite

class ScalaSerialDescriptorSpec extends FunSuite:
  test("Should create a ScalaSerialDescriptor for a case class") {
    final case class Person(age: Int, name: String) derives ScalaSerialDescriptor
    assert(Either.catchNonFatal(ScalaSerialDescriptor[Person].serialDescriptor).isRight)
  }