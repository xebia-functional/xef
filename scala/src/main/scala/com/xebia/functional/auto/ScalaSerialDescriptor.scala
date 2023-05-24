package com.xebia.functional.scala.auto

import com.xebia.functional.auto.KotlinXSerializers
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

  private inline def getSerialDescriptor[T <: Tuple]: List[SerialDescriptor] = inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (h *: t) =>
      summonInline[ClassTag[h]].runtimeClass match {
        case x if classOf[Boolean].isAssignableFrom(x) => KotlinXSerializers.boolean.getDescriptor :: getSerialDescriptor[t]
        case x if classOf[Byte].isAssignableFrom(x) => KotlinXSerializers.byte.getDescriptor :: getSerialDescriptor[t]
        case x if classOf[Char].isAssignableFrom(x) => KotlinXSerializers.char.getDescriptor :: getSerialDescriptor[t]
        case x if classOf[Double].isAssignableFrom(x) => KotlinXSerializers.double.getDescriptor :: getSerialDescriptor[t]
        case x if classOf[Float].isAssignableFrom(x) => KotlinXSerializers.float.getDescriptor :: getSerialDescriptor[t]
        case x if classOf[Int].isAssignableFrom(x) => KotlinXSerializers.int.getDescriptor :: getSerialDescriptor[t]
        case x if classOf[Long].isAssignableFrom(x) => KotlinXSerializers.long.getDescriptor :: getSerialDescriptor[t]
        case x if classOf[Short].isAssignableFrom(x) => KotlinXSerializers.short.getDescriptor :: getSerialDescriptor[t]
        case x if classOf[String].isAssignableFrom(x) => KotlinXSerializers.string.getDescriptor :: getSerialDescriptor[t]
        case _ => summonInline[ScalaSerialDescriptor[h]].serialDescriptor :: getSerialDescriptor[t]
      }

  inline final def derived[A](using inline m: Mirror.Of[A]): ScalaSerialDescriptor[A] = new ScalaSerialDescriptor[A]:
    val serialDescriptorImpl: SerialDescriptor = new SerialDescriptor:
      val labels = getElemsLabel[m.MirroredElemLabels]
      val serialDescriptors = getSerialDescriptor[m.MirroredElemTypes]

      override def getElementIndex(name: String): Int = labels.indexOf(name)

      // We're going to ignore annotations for now, it's not relevant for JsonSchema
      override def getElementAnnotations(index: Int): util.List[Annotation] = java.util.ArrayList(0)

      override def getElementDescriptor(index: Int): SerialDescriptor = serialDescriptors(index)

      // We're going to ignore annotations for now, it's not relevant for JsonSchema
      override def getAnnotations: util.List[Annotation] = java.util.ArrayList(0)

      override def getElementsCount: Int = labels.size

      override def isInline: Boolean = false

      // Is the element wrapped in `Option`, or is a union with `Null`?
      override def isNullable: Boolean = false

      override def getKind: SerialKind = StructureKind.CLASS.INSTANCE

      override def getSerialName: String = constValue[m.MirroredLabel]

      override def getElementName(i: Int): String = labels(i)

      // Does the element at the given index have a default value, or is it wrapped in `Option`, or is a union with `Null`?
      override def isElementOptional(index: Int): Boolean = false

    def serialDescriptor = serialDescriptorImpl
