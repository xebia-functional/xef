package com.xebia.functional.scala.auto

import com.xebia.functional.auto.KotlinXSerializers
import com.xebia.functional.scala.auto.ScalaSerialDescriptor

object ScalaSerialDescriptorContext:
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
