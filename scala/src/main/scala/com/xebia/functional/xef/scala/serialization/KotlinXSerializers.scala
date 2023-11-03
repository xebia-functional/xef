package com.xebia.functional.xef.scala.serialization

import kotlin.jvm.internal.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.BuiltinSerializersKt
import kotlinx.serialization.builtins.BuiltinSerializersKt.serializer

object KotlinXSerializers:
  val int: KSerializer[java.lang.Integer] = serializer(IntCompanionObject.INSTANCE)
  val string: KSerializer[String] = serializer(StringCompanionObject.INSTANCE)
  val boolean: KSerializer[java.lang.Boolean] = serializer(BooleanCompanionObject.INSTANCE)
  val double: KSerializer[java.lang.Double] = serializer(DoubleCompanionObject.INSTANCE)
  val float: KSerializer[java.lang.Float] = serializer(FloatCompanionObject.INSTANCE)
  val long: KSerializer[java.lang.Long] = serializer(LongCompanionObject.INSTANCE)
  val short: KSerializer[java.lang.Short] = serializer(ShortCompanionObject.INSTANCE)
  val byte: KSerializer[java.lang.Byte] = serializer(ByteCompanionObject.INSTANCE)
  val char: KSerializer[Character] = serializer(CharCompanionObject.INSTANCE)
  val unit: KSerializer[kotlin.Unit] = serializer(kotlin.Unit.INSTANCE)
