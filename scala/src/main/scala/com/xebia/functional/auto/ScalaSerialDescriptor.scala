package com.xebia.functional.scala.auto

import java.lang.annotation.Annotation
import kotlinx.serialization.descriptors.{SerialDescriptor, SerialKind, StructureKind}

import scala.deriving.*
import scala.compiletime.{constValue, erasedValue, summonInline}

object ScalaSerialDescriptor:
  inline def derived[A <: Product](using m: Mirror.Of[A]): SerialDescriptor =
    new SerialDescriptor:
      val serialName: String = constValue[m.MirroredLabel]
      val kind: SerialKind = StructureKind.CLASS.INSTANCE
      val elementsCount: Int = constValue[m.MirroredElemLabels].size

      def getElementIndex(name: String): Int = constValue[m.MirroredElemLabels].toArray.indexOf(name)

      def getElementAnnotations(index: Int): java.util.List[Annotation] = ??? // You need to determine how to get the Annotations

      def getElementDescriptor(index: Int): SerialDescriptor = ??? // You need to determine how to get the SerialDescriptor

      override def getAnnotations: java.util.List[Annotation] = ???

      override def getElementsCount: Int = ???

      override def isInline: Boolean = ???

      override def isNullable: Boolean = ???

      override def getKind: SerialKind = ???

      override def getSerialName: String = ???

      override def getElementName(i: Int): String = ???

      def isElementOptional(index: Int): Boolean = false // You need to determine how to check if element is optional