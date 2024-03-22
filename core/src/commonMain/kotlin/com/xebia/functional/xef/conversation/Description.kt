package com.xebia.functional.xef.conversation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Description of a property to enhance the LLM prompt chances to get the data in the right format
 */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class Description(val value: String)
