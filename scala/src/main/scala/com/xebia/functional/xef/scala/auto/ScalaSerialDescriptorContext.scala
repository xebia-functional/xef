package com.xebia.functional.xef.scala.auto

import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.BuiltinSerializersKt
import kotlinx.serialization.descriptors.SerialDescriptor

import scala.compiletime.summonInline
import scala.reflect.ClassTag

object ScalaSerialDescriptorContext:

  given [T: ScalaSerialDescriptor]: ScalaSerialDescriptor[Option[T]] = new ScalaSerialDescriptor[Option[T]]:
    def serialDescriptor = BuiltinSerializersKt.getNullable(ScalaSerialDescriptor[T].kserializer).getDescriptor

  given [T: ClassTag: ScalaSerialDescriptor]: ScalaSerialDescriptor[Array[T]] = new ScalaSerialDescriptor[Array[T]]:
    def serialDescriptor =
      val kClass = Reflection.createKotlinClass(summonInline[ClassTag[T]].runtimeClass).asInstanceOf[KClass[T]]
      BuiltinSerializersKt.ArraySerializer(kClass, ScalaSerialDescriptor[T].kserializer).getDescriptor

  given [T: ScalaSerialDescriptor]: ScalaSerialDescriptor[List[T]] = new ScalaSerialDescriptor[List[T]]:
    def serialDescriptor = BuiltinSerializersKt.ListSerializer(ScalaSerialDescriptor[T].kserializer).getDescriptor

  given [T: ScalaSerialDescriptor]: ScalaSerialDescriptor[Seq[T]] = new ScalaSerialDescriptor[Seq[T]]:
    def serialDescriptor = BuiltinSerializersKt.ListSerializer(ScalaSerialDescriptor[T].kserializer).getDescriptor

  given [T: ScalaSerialDescriptor]: ScalaSerialDescriptor[Vector[T]] = new ScalaSerialDescriptor[Vector[T]]:
    def serialDescriptor = BuiltinSerializersKt.ListSerializer(ScalaSerialDescriptor[T].kserializer).getDescriptor

  given [T: ScalaSerialDescriptor]: ScalaSerialDescriptor[Set[T]] = new ScalaSerialDescriptor[Set[T]]:
    def serialDescriptor = BuiltinSerializersKt.SetSerializer(ScalaSerialDescriptor[T].kserializer).getDescriptor

  given [K: ScalaSerialDescriptor, V: ScalaSerialDescriptor]: ScalaSerialDescriptor[Map[K, V]] = new ScalaSerialDescriptor[Map[K, V]]:
    def serialDescriptor =
      BuiltinSerializersKt.MapSerializer(ScalaSerialDescriptor[K].kserializer, ScalaSerialDescriptor[V].kserializer).getDescriptor

  given ScalaSerialDescriptor[Boolean] = new ScalaSerialDescriptor[Boolean]:
    def serialDescriptor = KotlinXSerializers.boolean.getDescriptor

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

  given ScalaSerialDescriptor[Unit] = new ScalaSerialDescriptor[Unit]:
    def serialDescriptor = KotlinXSerializers.unit.getDescriptor
