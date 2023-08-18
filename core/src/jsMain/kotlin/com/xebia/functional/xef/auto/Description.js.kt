package com.xebia.functional.xef.auto

import kotlinx.serialization.SerialInfo

/**
 * Description of a property to enhance the LLM prompt chances to get the data in the right format
 */
@SerialInfo
@Repeatable
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
actual annotation class Description actual constructor(actual val value: String)
