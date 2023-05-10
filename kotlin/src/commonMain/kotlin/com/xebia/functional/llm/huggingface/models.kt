package com.xebia.functional.llm.huggingface

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable data class Generation(val generatedText: String)

@Serializable data class InferenceRequest(val inputs: String, val maxLength: Int = 1000)

@Serializable @JvmInline value class Model(val name: String)
