package com.xebia.functional.xef.auto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Description of a property to enhance the LLM prompt chances to get the data in the right format
 */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Repeatable
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class Description(val lines: Array<out String>)
