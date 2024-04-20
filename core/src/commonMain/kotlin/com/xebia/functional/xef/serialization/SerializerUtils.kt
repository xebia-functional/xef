@file:OptIn(ExperimentalSerializationApi::class)

package com.xebia.functional.xef.serialization

import com.xebia.functional.xef.llm.StreamedFunction
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

inline fun <reified T: Any> Serializer(): Serializer<T> {
  val kserializer = serializer<T>()
  return fromKotlinSerializer<T>(kserializer)
}

inline fun <reified T : Any> fromKotlinSerializer(kserializer: KSerializer<T>): Serializer<T> {
 return object : Serializer<T> {
  override fun serialize(value: T): String = Json.encodeToString(kserializer, value)
  override fun deserialize(value: String): T = Json.decodeFromString(kserializer, value)
  override val name: String = kserializer.descriptor.serialName
  override val schema: String = kserializer.descriptor.serialName
  override val kind: SerialKind = kserializer.descriptor.kind
  override fun elements(): List<Serializer<*>> = kserializer.descriptor.elementDescriptors.map { fromKotlinSerializer(it) }
  override fun cases(): List<Serializer<*>> = TODO()
  override val isNullable: Boolean = kserializer.descriptor.isNullable
  override val elementsCount: Int = kserializer.descriptor.elementsCount
  override fun annotations(): List<Annotation> = kserializer.descriptor.annotations
  override fun elementNames(): List<String> = kserializer.descriptor.elementNames.toList()
  override val isFlowOfString: Boolean = typeOf<T>() == typeOf<Flow<String>>()
  override val isFlowOfFunction: Boolean = typeOf<T>() == typeOf<Flow<StreamedFunction<*>>>()
  override val flowOfFunctionSerializer: Serializer<*>? = typeOf<T>().arguments.firstOrNull()?.type?.classifier?.let {
    (it as KClass<*>).serializerOrNull()
  }
}

expect fun <T: Any> KClass<T>.serializerOrNull(): Serializer<T>
