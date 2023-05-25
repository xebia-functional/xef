package com.xebia.functional.scala.auto

import com.xebia.functional.auto.KotlinXSerializers
import com.xebia.functional.scala.auto.ScalaSerialDescriptor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.BuiltinSerializersKt

import scala.compiletime.summonInline
import scala.reflect.ClassTag

object ScalaSerialDescriptorContext:
  given [T: ClassTag]: KSerializer[T] = (summonInline[ClassTag[T]].runtimeClass match {
    case x if classOf[String].isAssignableFrom(x) => KotlinXSerializers.string
    case x if classOf[Boolean].isAssignableFrom(x) => KotlinXSerializers.boolean
    case x if classOf[Byte].isAssignableFrom(x) => KotlinXSerializers.byte
    case x if classOf[Char].isAssignableFrom(x) => KotlinXSerializers.char
    case x if classOf[Double].isAssignableFrom(x) => KotlinXSerializers.double
    case x if classOf[Float].isAssignableFrom(x) => KotlinXSerializers.float
    case x if classOf[Int].isAssignableFrom(x) => KotlinXSerializers.int
    case x if classOf[Long].isAssignableFrom(x) => KotlinXSerializers.long
    case x if classOf[Short].isAssignableFrom(x) => KotlinXSerializers.short
  }).asInstanceOf[KSerializer[T]]
  given [T: ClassTag]: ScalaSerialDescriptor[Option[T]] = new ScalaSerialDescriptor[Option[T]]:
    def serialDescriptor = BuiltinSerializersKt.getNullable(summonInline[KSerializer[T]]).getDescriptor

  given [T: ClassTag]: ScalaSerialDescriptor[List[T]] = new ScalaSerialDescriptor[List[T]]:
    def serialDescriptor = BuiltinSerializersKt.ListSerializer(summonInline[KSerializer[T]]).getDescriptor

  given [T: ClassTag]: ScalaSerialDescriptor[Set[T]] = new ScalaSerialDescriptor[Set[T]]:
    def serialDescriptor = BuiltinSerializersKt.SetSerializer(summonInline[KSerializer[T]]).getDescriptor

  given [S: ClassTag, T: ClassTag]: ScalaSerialDescriptor[Map[S, T]] = new ScalaSerialDescriptor[Map[S, T]]:
    def serialDescriptor = BuiltinSerializersKt.MapSerializer(summonInline[KSerializer[T]], summonInline[KSerializer[T]]).getDescriptor

  given ScalaSerialDescriptor[Byte] = new ScalaSerialDescriptor[Byte]:
    def serialDescriptor = KotlinXSerializers.byte.getDescriptor

  given ScalaSerialDescriptor[Char] = new ScalaSerialDescriptor[Char]:
    def serialDescriptor = KotlinXSerializers.char.getDescriptor

  given ScalaSerialDescriptor[Double] = new ScalaSerialDescriptor[Double]:
    def serialDescriptor = KotlinXSerializers.double.getDescriptor

  given ScalaSerialDescriptor[Float] = new ScalaSerialDescriptor[Float]:
    def serialDescriptor = KotlinXSerializers.float.getDescriptor

  given ScalaSerialDescriptor[Int] = new ScalaSerialDescriptor[Int]:
    def serialDescriptor = KotlinXSerializers.int.getDescriptor

  given ScalaSerialDescriptor[Long] = new ScalaSerialDescriptor[Long]:
    def serialDescriptor = KotlinXSerializers.long.getDescriptor

  given ScalaSerialDescriptor[Short] = new ScalaSerialDescriptor[Short]:
    def serialDescriptor = KotlinXSerializers.short.getDescriptor

  given ScalaSerialDescriptor[String] = new ScalaSerialDescriptor[String]:
    def serialDescriptor = KotlinXSerializers.string.getDescriptor
