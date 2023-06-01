package com.xebia.functional.xef.scala.auto

import kotlinx.serialization.descriptors.{SerialDescriptor, SerialKind, StructureKind}
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.{Decoder, Encoder}

import java.lang.annotation.Annotation
import java.util
import scala.compiletime.{constValue, erasedValue, summonInline}
import scala.deriving.*

trait ScalaSerialDescriptor[A]:
  def serialDescriptor: SerialDescriptor
  def kserializer: KSerializer[A] = new KSerializer[A]:
    override def getDescriptor: SerialDescriptor = serialDescriptor
    override def serialize(encoder: Encoder, t: A): Unit = ???
    override def deserialize(decoder: Decoder): A = ???

object ScalaSerialDescriptor:
  def apply[A](using ev: ScalaSerialDescriptor[A]): ScalaSerialDescriptor[A] = ev

  private inline def getElemsLabel[T <: Tuple]: List[String] = inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (h *: t) => erasedValue[h].toString :: getElemsLabel[t]

  private inline def getSerialDescriptor[T <: Tuple]: List[SerialDescriptor] = inline erasedValue[T] match
    case _: EmptyTuple => Nil
    case _: (String *: t) => KotlinXSerializers.string.getDescriptor :: getSerialDescriptor[t]
    case _: (Boolean *: t) => KotlinXSerializers.boolean.getDescriptor :: getSerialDescriptor[t]
    case _: (Byte *: t) => KotlinXSerializers.byte.getDescriptor :: getSerialDescriptor[t]
    case _: (Char *: t) => KotlinXSerializers.char.getDescriptor :: getSerialDescriptor[t]
    case _: (Double *: t) => KotlinXSerializers.double.getDescriptor :: getSerialDescriptor[t]
    case _: (Float *: t) => KotlinXSerializers.float.getDescriptor :: getSerialDescriptor[t]
    case _: (Int *: t) => KotlinXSerializers.int.getDescriptor :: getSerialDescriptor[t]
    case _: (Long *: t) => KotlinXSerializers.long.getDescriptor :: getSerialDescriptor[t]
    case _: (Short *: t) => KotlinXSerializers.short.getDescriptor :: getSerialDescriptor[t]
    case _: (Unit *: t) => KotlinXSerializers.unit.getDescriptor :: getSerialDescriptor[t]
    case _: (h *: t) => summonInline[ScalaSerialDescriptor[h]].serialDescriptor :: getSerialDescriptor[t]

  inline final def derived[A](using m: Mirror.Of[A]): ScalaSerialDescriptor[A] = new ScalaSerialDescriptor[A]:
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
