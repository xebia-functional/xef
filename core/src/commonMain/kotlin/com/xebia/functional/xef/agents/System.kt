package com.xebia.functional.xef.agents

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlin.reflect.KClass

@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class System(
  vararg val instructions: String,
  val tools: Array<KClass<*>> = []
)
