package com.xebia.functional.xef.scala.auto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.BuiltinSerializersKt
import kotlinx.serialization.builtins.BuiltinSerializersKt.serializer

import java.lang

object KotlinXSerializers:
  val int: KSerializer[Integer] =
    serializer(kotlin.jvm.internal.IntCompanionObject.INSTANCE)

  val string: KSerializer[String] =
    serializer(kotlin.jvm.internal.StringCompanionObject.INSTANCE)

  val boolean: KSerializer[lang.Boolean] =
    serializer(kotlin.jvm.internal.BooleanCompanionObject.INSTANCE)

  val double: KSerializer[lang.Double] =
    serializer(kotlin.jvm.internal.DoubleCompanionObject.INSTANCE)

  val float: KSerializer[lang.Float] =
    serializer(kotlin.jvm.internal.FloatCompanionObject.INSTANCE)

  val long: KSerializer[lang.Long] =
    serializer(kotlin.jvm.internal.LongCompanionObject.INSTANCE)

  val short: KSerializer[lang.Short] =
    serializer(kotlin.jvm.internal.ShortCompanionObject.INSTANCE)

  val byte: KSerializer[lang.Byte] =
    serializer(kotlin.jvm.internal.ByteCompanionObject.INSTANCE)

  val char: KSerializer[Character] =
    serializer(kotlin.jvm.internal.CharCompanionObject.INSTANCE)

  val unit: KSerializer[kotlin.Unit] = serializer(kotlin.Unit.INSTANCE)
