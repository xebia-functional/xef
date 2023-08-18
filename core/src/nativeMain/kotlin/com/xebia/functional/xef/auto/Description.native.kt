package com.xebia.functional.xef.auto

/**
 * Description of a property to enhance the LLM prompt chances to get the data in the right format
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
actual annotation class Description actual constructor(actual val value: String)
