//package com.xebia.functional.tokenizer
//
//import com.xebia.functional.tokenizer.EncodingType.CL100K_BASE
//import com.xebia.functional.tokenizer.EncodingType.P50K_BASE
//import com.xebia.functional.tokenizer.EncodingType.R50K_BASE
//import kotlin.jvm.JvmStatic
//
///**
// * Formal description of a model and it's properties
// * without any capabilities.
// */
//open class ModelType(
//    /**
//     * Returns the name of the model type as used by the OpenAI API.
//     *
//     * @return the name of the model type
//     */
//    open val name: String,
//
//    open val maxInputTokens: Int,
//    open val maxOutputTokens: Int,
//
//    open val tokensPerMessage: Int = 0,
//    open val tokensPerName: Int = 0,
//    open val tokenPadding: Int = 20,
//) {
//
//    /**
//     * Returns the maximum context length that is supported by this model type. Note that
//     * the maximum context length consists of the amount of prompt tokens and the amount of
//     * completion tokens (where applicable).
//     *
//     * @return the maximum context length for this model type
//     */
//    val maxContextLength: Int get() = maxInputTokens + maxOutputTokens
//
//    val encoding: Encoding get() = TODO()
//
//    data class LocalModel(override val name: String, override val maxContextLength: Int) :
//        ModelType(name, maxContextLength)
//
//}
