package com.xebia.functional.openai.apis

import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializerOrNull

@OptIn(InternalAPI::class)
fun <T : Any> FormBuilder.appendGen(key: String, value: T, headers: Headers = Headers.Empty): Unit {
  when (value) {
    is String -> append(key, value, headers)
    is Number -> append(key, value, headers)
    is Boolean -> append(key, value, headers)
    is ByteArray -> append(key, value, headers)
    is ByteReadPacket -> append(key, value, headers)
    is InputProvider -> append(key, value, headers)
    is ChannelProvider -> append(key, value, headers)
    is Enum<*> -> append(key, serialNameOrEnumValue(value), headers)
    else -> append(key, value, headers)
  }
}

@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
fun <T : Enum<T>> serialNameOrEnumValue(v: Enum<T>): String =
  v::class.serializerOrNull()?.descriptor?.getElementName(v.ordinal) ?: v.toString()
