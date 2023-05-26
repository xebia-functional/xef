package com.xebia.functional.scala.auto

import cats.syntax.either.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.BuiltinSerializersKt
import munit.FunSuite

import scala.collection.immutable.HashSet
import scala.compiletime.summonInline
import scala.reflect.ClassTag

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

  test("Should create a ScalaSerialDescriptor for a simple case class with seq and vector fields") {
    final case class Person(age: Int, name: String, other1: Seq[Byte], other2: Vector[Short]) derives ScalaSerialDescriptor
    assert(Either.catchNonFatal(ScalaSerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a ScalaSerialDescriptor for a simple case class with an array field") {
    final case class Person(other: Array[Double]) derives ScalaSerialDescriptor
    assert(Either.catchNonFatal(ScalaSerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a ScalaSerialDescriptor for a simple case class with map fields") {
    final case class Person(age: Int, name: String, alias: Map[String, String]) derives ScalaSerialDescriptor
    assert(Either.catchNonFatal(ScalaSerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a ScalaSerialDescriptor for a simple case class with HashSet field, providing a custom given") {
    given [T: ClassTag]: ScalaSerialDescriptor[HashSet[T]] = new ScalaSerialDescriptor[HashSet[T]]:
      def serialDescriptor = BuiltinSerializersKt.SetSerializer(summonInline[KSerializer[T]]).getDescriptor
    final case class Person(age: Int, name: String, alias: HashSet[String]) derives ScalaSerialDescriptor
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
