package com.xebia.functional.xef.scala.auto

import cats.syntax.either.*
import kotlinx.serialization.builtins.BuiltinSerializersKt
import munit.FunSuite

import scala.collection.immutable.HashSet
import scala.compiletime.summonInline
import scala.reflect.ClassTag
import java.lang.annotation.Annotation
import scala.annotation.StaticAnnotation
import scala.jdk.CollectionConverters.*
import java.lang.{annotation => jla}

class SerialDescriptorSpec extends FunSuite:

  test("Should create a SerialDescriptor for a simple case class") {
    final case class Person(age: Int, name: String) derives SerialDescriptor
    assert(Either.catchNonFatal(SerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a SerialDescriptor for a simple case class with optional fields") {
    final case class Person(age: Int, name: String, id: Option[Long]) derives SerialDescriptor
    assert(Either.catchNonFatal(SerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a SerialDescriptor for a simple case class with set and list fields") {
    final case class Person(age: Int, name: String, siblingNames: Set[String], nationality: List[String]) derives SerialDescriptor
    assert(Either.catchNonFatal(SerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a SerialDescriptor for a simple case class with a list case class") {
    final case class Pet(age: Int, name: String) derives SerialDescriptor
    final case class Person(age: Int, name: String, pets: List[Pet]) derives SerialDescriptor

    assert(Either.catchNonFatal(SerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a SerialDescriptor for a simple case class with seq and vector fields") {
    final case class Person(age: Int, name: String, other1: Seq[Byte], other2: Vector[Short]) derives SerialDescriptor
    assert(Either.catchNonFatal(SerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a SerialDescriptor for a simple case class with an array field") {
    final case class Person(other: Array[Double]) derives SerialDescriptor
    assert(Either.catchNonFatal(SerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a SerialDescriptor for a simple case class with map fields") {
    final case class Person(age: Int, name: String, alias: Map[String, String]) derives SerialDescriptor
    assert(Either.catchNonFatal(SerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a SerialDescriptor for a simple case class with HashSet field, providing a custom given") {
    given [T: SerialDescriptor]: SerialDescriptor[HashSet[T]] = new SerialDescriptor[HashSet[T]]:
      def serialDescriptor = BuiltinSerializersKt.SetSerializer(SerialDescriptor[T].kserializer).getDescriptor
    final case class Person(age: Int, name: String, alias: HashSet[String]) derives SerialDescriptor
    assert(Either.catchNonFatal(SerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a SerialDescriptor for a composite case class") {
    final case class Person(age: Int, name: PersonName) derives SerialDescriptor
    final case class PersonName(firstName: String, lastName: String) derives SerialDescriptor
    assert(Either.catchNonFatal(SerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should create a SerialDescriptor for a composite (level 2) case class") {
    final case class Person(age: Int, placeOfBirth: PlaceOfBirth) derives SerialDescriptor
    final case class PlaceOfBirth(city: String, country: Country) derives SerialDescriptor
    final case class Country(country: String) derives SerialDescriptor
    assert(Either.catchNonFatal(SerialDescriptor[Person].serialDescriptor).isRight)
  }

  test("Should capture the annotations for an element"):
    case class MyAnnotation(name: String) extends StaticAnnotation

    final case class AnnotatedPerson(@MyAnnotation("firstName") @TestJavaAnnotation(name = "givenName") name: String) derives SerialDescriptor
    assert(Either.catchNonFatal(SerialDescriptor[AnnotatedPerson].serialDescriptor).isRight)
    assertEquals(
      Either
        .catchNonFatal:
          SerialDescriptor[AnnotatedPerson]
        .map: sd =>
          sd.serialDescriptor
            .getElementAnnotations(0)
            .asScala.toList.map: (a: jla.Annotation @unchecked) =>
              if (a.isInstanceOf[WrappedAnnotation[MyAnnotation]])
                a.asInstanceOf[WrappedAnnotation[MyAnnotation]].a.name
              else
                a.asInstanceOf[TestJavaAnnotation].name(),
      Right(List("givenName", "firstName"))
    )

  test("Should capture the annotations for a subtype of a sum type"):
    case class MyAnnotation(name: String) extends StaticAnnotation
    sealed trait Color derives SerialDescriptor
    @MyAnnotation("favorite") case object Red extends Color derives SerialDescriptor
    case object Green extends Color derives SerialDescriptor
    case object Blue extends Color derives SerialDescriptor
    assert(Either.catchNonFatal(SerialDescriptor[Color].serialDescriptor).isRight)
    assertEquals(
      Either
        .catchNonFatal:
          SerialDescriptor[Color]
        .map: sd =>
          sd.serialDescriptor
            .getElementAnnotations(0)
            .asScala.toList.map: (a: jla.Annotation @unchecked) =>
              if (a.isInstanceOf[WrappedAnnotation[MyAnnotation]])
                a.asInstanceOf[WrappedAnnotation[MyAnnotation]].a.name
              else
                a.asInstanceOf[TestJavaAnnotation].name(),
      Right(List("favorite"))
    )
