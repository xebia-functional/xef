package com.xebia.functional.scala.auto

import kotlinx.serialization.descriptors.SerialDescriptorsKt.PrimitiveSerialDescriptor

import java.lang.annotation.Annotation
import kotlinx.serialization.descriptors.{PrimitiveKind, SerialDescriptor, SerialKind, StructureKind}
import kotlinx.serialization.internal.ArrayListSerializer

import java.util
import scala.deriving.*
import scala.compiletime.{constValue, erasedValue, summonInline}
import scala.reflect.ClassTag

trait ScalaSerialDescriptor[A]:
  def serialDescriptor: SerialDescriptor

object ScalaSerialDescriptor:
  def apply[A](using ev: ScalaSerialDescriptor[A]): ScalaSerialDescriptor[A] = ev

  private inline def getElemsLabel[T <: Tuple]: List[String] = inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (h *: t) => erasedValue[h].toString :: getElemsLabel[t]

  private inline def getElemTypes[T <: Tuple]: List[Class[_]] = inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (h *: t) => summonInline[ClassTag[h]].runtimeClass :: getElemTypes[t]

  inline final def derived[A](using inline m: Mirror.Of[A]): ScalaSerialDescriptor[A] = new ScalaSerialDescriptor[A]:
    val serialDescriptorImpl: SerialDescriptor = new SerialDescriptor:
      def getElementIndex(name: String): Int = getElemsLabel[m.MirroredElemLabels].indexOf(name)

      // We're going to ignore annotations for now, it's not relevant for JsonSchema
      def getElementAnnotations(index: Int): util.List[Annotation] = java.util.ArrayList(0)

      def getElementDescriptor(index: Int): SerialDescriptor =
        val types = getElemTypes[m.MirroredElemTypes].map(_.toString)
        println(s"TYPEEEEEES: $types")
        getElemTypes[m.MirroredElemTypes](index).toString match {
          case s if s.toLowerCase.contains("string") => PrimitiveSerialDescriptor("String", PrimitiveKind.STRING.INSTANCE)
          case s if s.toLowerCase.contains("int") => PrimitiveSerialDescriptor("Int", PrimitiveKind.INT.INSTANCE)
          case s if s.toLowerCase.contains("long") => PrimitiveSerialDescriptor("Long", PrimitiveKind.LONG.INSTANCE)
          case s if s.toLowerCase.contains("float") => PrimitiveSerialDescriptor("Float", PrimitiveKind.FLOAT.INSTANCE)
          case s if s.toLowerCase.contains("double") => PrimitiveSerialDescriptor("Double", PrimitiveKind.DOUBLE.INSTANCE)
          case s if s.toLowerCase.contains("boolean") => PrimitiveSerialDescriptor("Boolean", PrimitiveKind.BOOLEAN.INSTANCE)
          case s if s.toLowerCase.contains("byte") => PrimitiveSerialDescriptor("Byte", PrimitiveKind.BYTE.INSTANCE)
          case s if s.toLowerCase.contains("short") => PrimitiveSerialDescriptor("Short", PrimitiveKind.SHORT.INSTANCE)
          case s if s.toLowerCase.contains("char") => PrimitiveSerialDescriptor("Char", PrimitiveKind.CHAR.INSTANCE)
        }

      // We're going to ignore annotations for now, it's not relevant for JsonSchema
      override def getAnnotations: util.List[Annotation] = java.util.ArrayList(0)

      override def getElementsCount: Int = getElemsLabel[m.MirroredElemLabels].size

      override def isInline: Boolean = false

      // Is the element wrapped in `Option`, or is a union with `Null`?
      override def isNullable: Boolean = false

      override def getKind: SerialKind = StructureKind.CLASS.INSTANCE

      override def getSerialName: String = constValue[m.MirroredLabel]

      override def getElementName(i: Int): String = getElemsLabel[m.MirroredElemLabels](i)

      // Does the element at the given index have a default value, or is it wrapped in `Option`, or is a union with `Null`?
      def isElementOptional(index: Int): Boolean = false

    def serialDescriptor = serialDescriptorImpl