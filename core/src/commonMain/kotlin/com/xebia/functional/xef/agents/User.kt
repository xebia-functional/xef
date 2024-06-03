package com.xebia.functional.xef.agents

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlin.reflect.KClass

@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class User(
  vararg val prompt: String,
  val tools: Array<KClass<*>> = [],
  val temperature: Double = 0.0,

)
