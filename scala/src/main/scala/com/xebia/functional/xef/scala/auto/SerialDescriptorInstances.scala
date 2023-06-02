package com.xebia.functional.xef.scala.auto

import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlinx.serialization.builtins.BuiltinSerializersKt

import scala.compiletime.summonInline
import scala.reflect.ClassTag

class SerialDescriptorInstances:

  given [T: SerialDescriptor]: SerialDescriptor[Option[T]] = new SerialDescriptor[Option[T]]:
    def serialDescriptor = BuiltinSerializersKt.getNullable(SerialDescriptor[T].kserializer).getDescriptor

  given [T: ClassTag: SerialDescriptor]: SerialDescriptor[Array[T]] = new SerialDescriptor[Array[T]]:
    def serialDescriptor =
      val kClass = Reflection.createKotlinClass(summonInline[ClassTag[T]].runtimeClass).asInstanceOf[KClass[T]]
      BuiltinSerializersKt.ArraySerializer(kClass, SerialDescriptor[T].kserializer).getDescriptor

  given [T: SerialDescriptor]: SerialDescriptor[List[T]] = new SerialDescriptor[List[T]]:
    def serialDescriptor = BuiltinSerializersKt.ListSerializer(SerialDescriptor[T].kserializer).getDescriptor

  given [T: SerialDescriptor]: SerialDescriptor[Seq[T]] = new SerialDescriptor[Seq[T]]:
    def serialDescriptor = BuiltinSerializersKt.ListSerializer(SerialDescriptor[T].kserializer).getDescriptor

  given [T: SerialDescriptor]: SerialDescriptor[Vector[T]] = new SerialDescriptor[Vector[T]]:
    def serialDescriptor = BuiltinSerializersKt.ListSerializer(SerialDescriptor[T].kserializer).getDescriptor

  given [T: SerialDescriptor]: SerialDescriptor[Set[T]] = new SerialDescriptor[Set[T]]:
    def serialDescriptor = BuiltinSerializersKt.SetSerializer(SerialDescriptor[T].kserializer).getDescriptor

  given [K: SerialDescriptor, V: SerialDescriptor]: SerialDescriptor[Map[K, V]] = new SerialDescriptor[Map[K, V]]:
    def serialDescriptor =
      BuiltinSerializersKt.MapSerializer(SerialDescriptor[K].kserializer, SerialDescriptor[V].kserializer).getDescriptor

  given SerialDescriptor[Boolean] = new SerialDescriptor[Boolean]:
    def serialDescriptor = KotlinXSerializers.boolean.getDescriptor

  given SerialDescriptor[Byte] = new SerialDescriptor[Byte]:
    def serialDescriptor = KotlinXSerializers.byte.getDescriptor

  given SerialDescriptor[Char] = new SerialDescriptor[Char]:
    def serialDescriptor = KotlinXSerializers.char.getDescriptor

  given SerialDescriptor[Double] = new SerialDescriptor[Double]:
    def serialDescriptor = KotlinXSerializers.double.getDescriptor

  given SerialDescriptor[Float] = new SerialDescriptor[Float]:
    def serialDescriptor = KotlinXSerializers.float.getDescriptor

  given SerialDescriptor[Int] = new SerialDescriptor[Int]:
    def serialDescriptor = KotlinXSerializers.int.getDescriptor

  given SerialDescriptor[Long] = new SerialDescriptor[Long]:
    def serialDescriptor = KotlinXSerializers.long.getDescriptor

  given SerialDescriptor[Short] = new SerialDescriptor[Short]:
    def serialDescriptor = KotlinXSerializers.short.getDescriptor

  given SerialDescriptor[String] = new SerialDescriptor[String]:
    def serialDescriptor = KotlinXSerializers.string.getDescriptor

  given SerialDescriptor[Unit] = new SerialDescriptor[Unit]:
    def serialDescriptor = KotlinXSerializers.unit.getDescriptor
