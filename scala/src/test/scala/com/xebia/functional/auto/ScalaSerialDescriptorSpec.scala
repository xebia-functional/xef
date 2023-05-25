package com.xebia.functional.scala.auto

import cats.syntax.either.*
import munit.FunSuite

class ScalaSerialDescriptorSpec extends FunSuite:
  import ScalaSerialDescriptorContext.given

  test("Should create a ScalaSerialDescriptor for a simple case class") {
    final case class Person(age: Int, name: String) derives ScalaSerialDescriptor
    assert(Either.catchNonFatal(ScalaSerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a ScalaSerialDescriptor for a simple case class with optional fields") {
    final case class Person(age: Int, name: String, id: Option[Long]) derives ScalaSerialDescriptor
    assert(Either.catchNonFatal(ScalaSerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a ScalaSerialDescriptor for a simple case class with set and list fields") {
    final case class Person(age: Int, name: String, siblingNames: Set[String], nationality: List[String]) derives ScalaSerialDescriptor
    assert(Either.catchNonFatal(ScalaSerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a ScalaSerialDescriptor for a simple case class with map fields") {
    final case class Person(age: Int, name: String, alias: Map[String, String]) derives ScalaSerialDescriptor
    assert(Either.catchNonFatal(ScalaSerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a ScalaSerialDescriptor for a composite case class") {
    final case class Person(age: Int, name: PersonName) derives ScalaSerialDescriptor
    final case class PersonName(firstName: String, lastName: String) derives ScalaSerialDescriptor
    assert(Either.catchNonFatal(ScalaSerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a ScalaSerialDescriptor for a composite (level 2) case class") {
    final case class Person(age: Int, placeOfBirth: PlaceOfBirth) derives ScalaSerialDescriptor
    final case class PlaceOfBirth(city: String, country: Country) derives ScalaSerialDescriptor
    final case class Country(country: String) derives ScalaSerialDescriptor
    assert(Either.catchNonFatal(ScalaSerialDescriptor[Person].serialDescriptor).isRight)
  }
