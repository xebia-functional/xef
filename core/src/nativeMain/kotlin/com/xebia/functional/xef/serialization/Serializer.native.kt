package com.xebia.functional.xef.serialization

import kotlin.reflect.KClass

actual fun <T : Any> KClass<T>.serializerOrNull(): Serializer<T> {
  TODO("Not yet implemented")
}
