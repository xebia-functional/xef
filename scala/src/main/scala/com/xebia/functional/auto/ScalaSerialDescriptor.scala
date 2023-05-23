package com.xebia.functional.scala.auto

import java.lang.annotation.Annotation
import kotlinx.serialization.descriptors.{SerialDescriptor, SerialKind, StructureKind}

import scala.deriving.*
import scala.compiletime.{constValue, erasedValue}

object ScalaSerialDescriptor:
  inline def derived[A <: Product](using m: Mirror.Of[A]): SerialDescriptor =
    new SerialDescriptor:
      def getElementIndex(name: String): Int = constValue[m.MirroredElemLabels].toArray.indexOf(name)

      def getElementAnnotations(index: Int): java.util.List[Annotation] = ???

      def getElementDescriptor(index: Int): SerialDescriptor = ??? // You need to determine how to get the SerialDescriptor

      override def getAnnotations: java.util.List[Annotation] = ???

      override def getElementsCount: Int = constValue[m.MirroredElemLabels].size

      override def isInline: Boolean = false

      override def isNullable: Boolean = false

      override def getKind: SerialKind = StructureKind.CLASS.INSTANCE

      override def getSerialName: String = constValue[m.MirroredLabel]

      override def getElementName(i: Int): String = constValue[m.MirroredElemLabels].productElementName(i)

      def isElementOptional(index: Int): Boolean = false // You need to determine how to check if element is optional