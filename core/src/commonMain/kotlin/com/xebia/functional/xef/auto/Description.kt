package com.xebia.functional.xef.auto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Description of a property to enhance the LLM prompt chances to get the data in the right format
 */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
expect annotation class Description(val value: String)
