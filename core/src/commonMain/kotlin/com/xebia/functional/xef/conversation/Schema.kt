package com.xebia.functional.xef.conversation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/** Schema for a tool request */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Schema(val value: String)
