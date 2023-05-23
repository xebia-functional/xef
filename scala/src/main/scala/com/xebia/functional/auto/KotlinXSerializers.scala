package com.xebia.functional.auto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.BuiltinSerializersKt.serializer

import java.lang

object KotlinXSerializers:
  def int: KSerializer[Integer] =
    serializer(kotlin.jvm.internal.IntCompanionObject.INSTANCE)

  def string: KSerializer[String] =
    serializer(kotlin.jvm.internal.StringCompanionObject.INSTANCE)

  def boolean: KSerializer[lang.Boolean] =
    serializer(kotlin.jvm.internal.BooleanCompanionObject.INSTANCE)

  def double: KSerializer[lang.Double] =
    serializer(kotlin.jvm.internal.DoubleCompanionObject.INSTANCE)

  def float: KSerializer[lang.Float] =
    serializer(kotlin.jvm.internal.FloatCompanionObject.INSTANCE)

  def long: KSerializer[lang.Long] =
    serializer(kotlin.jvm.internal.LongCompanionObject.INSTANCE)

  def short: KSerializer[lang.Short] =
    serializer(kotlin.jvm.internal.ShortCompanionObject.INSTANCE)

  def byte: KSerializer[lang.Byte] =
    serializer(kotlin.jvm.internal.ByteCompanionObject.INSTANCE)

  def char: KSerializer[Character] =
    serializer(kotlin.jvm.internal.CharCompanionObject.INSTANCE)
