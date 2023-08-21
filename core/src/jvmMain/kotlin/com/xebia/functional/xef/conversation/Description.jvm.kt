package com.xebia.functional.xef.conversation

import com.xebia.functional.xef.conversation.jvm.Description
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/**
 * Description of a property to enhance the LLM prompt chances to get the data in the right format
 */

/**
 * Description of a property to enhance the LLM prompt chances to get the data in the right format
 */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
actual annotation class Description actual constructor(actual val value: String)

actual typealias Descriptive = Description // ref to the Java class jvm.Description
