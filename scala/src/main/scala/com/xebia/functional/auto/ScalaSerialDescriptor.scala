package com.xebia.functional.scala.auto

import kotlinx.serialization.descriptors.SerialDescriptorsKt.PrimitiveSerialDescriptor

import java.lang.annotation.Annotation
import kotlinx.serialization.descriptors.{PrimitiveKind, SerialDescriptor, SerialKind, StructureKind}
import kotlinx.serialization.internal.ArrayListSerializer

import scala.deriving.*
import scala.compiletime.{constValue, erasedValue}

object ScalaSerialDescriptor:
  inline def derived[A <: Product](using m: Mirror.Of[A]): SerialDescriptor =
    new SerialDescriptor:
      def getElementIndex(name: String): Int = constValue[m.MirroredElemLabels].toArray.indexOf(name)

      // We're going to ignore annotations for now, it's not relevant for JsonSchema
      def getElementAnnotations(index: Int): java.util.List[Annotation] = java.util.ArrayList(0)

      def getElementDescriptor(index: Int): SerialDescriptor =
        constValue[m.MirroredElemTypes].productElementName(index) match {
          case "String" => PrimitiveSerialDescriptor("String", PrimitiveKind.STRING.INSTANCE)
          case "Int" => PrimitiveSerialDescriptor("Int", PrimitiveKind.INT.INSTANCE)
          case "Long" => PrimitiveSerialDescriptor("Long", PrimitiveKind.LONG.INSTANCE)
          case "Float" => PrimitiveSerialDescriptor("Float", PrimitiveKind.FLOAT.INSTANCE)
          case "Double" => PrimitiveSerialDescriptor("Double", PrimitiveKind.DOUBLE.INSTANCE)
          case "Boolean" => PrimitiveSerialDescriptor("Boolean", PrimitiveKind.BOOLEAN.INSTANCE)
          case "Byte" => PrimitiveSerialDescriptor("Byte", PrimitiveKind.BYTE.INSTANCE)
          case "Short" => PrimitiveSerialDescriptor("Short", PrimitiveKind.SHORT.INSTANCE)
          case "Char" => PrimitiveSerialDescriptor("Char", PrimitiveKind.CHAR.INSTANCE)
        }

      // We're going to ignore annotations for now, it's not relevant for JsonSchema
      override def getAnnotations: java.util.List[Annotation] = java.util.ArrayList(0)

      override def getElementsCount: Int = constValue[m.MirroredElemLabels].size

      override def isInline: Boolean = false

      // Is the element wrapped in `Option`, or is a union with `Null`?
      override def isNullable: Boolean = false

      override def getKind: SerialKind = StructureKind.CLASS.INSTANCE

      override def getSerialName: String = constValue[m.MirroredLabel]

      override def getElementName(i: Int): String = constValue[m.MirroredElemLabels].productElementName(i)

      // Does the element at the given index have a default value, or is it wrapped in `Option`, or is a union with `Null`?
      def isElementOptional(index: Int): Boolean = false