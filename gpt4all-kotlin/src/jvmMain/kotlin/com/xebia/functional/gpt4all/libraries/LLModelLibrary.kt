package com.xebia.functional.gpt4all.libraries

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.platform.unix.LibCAPI

@Structure.FieldOrder(
    "logits",
    "logits_size",
    "tokens",
    "tokens_size",
    "n_past",
    "n_ctx",
    "n_predict",
    "top_k",
    "top_p",
    "temp",
    "n_batch",
    "repeat_penalty",
    "repeat_last_n",
    "context_erase"
)
open class LLModelContext(
    @JvmField
    var logits: Pointer? = null,
    @JvmField
    var logits_size: LibCAPI.size_t? = null,
    @JvmField
    var tokens: Pointer? = null,
    @JvmField
    var tokens_size: LibCAPI.size_t? = null,
    @JvmField
    var n_past: Int = 0,
    @JvmField
    var n_ctx: Int = 0,
    @JvmField
    var n_predict: Int = 0,
    @JvmField
    var top_k: Int = 0,
    @JvmField
    var top_p: Float = 0f,
    @JvmField
    var temp: Float = 0f,
    @JvmField
    var n_batch: Int = 0,
    @JvmField
    var repeat_penalty: Float = 0f,
    @JvmField
    var repeat_last_n: Int = 0,
    @JvmField
    var context_erase: Float = 0f
) : Structure()

sealed interface LLModelLibrary : Library {
    fun llmodel_loadModel(model: Pointer?, modelPath: String?): Boolean
    fun llmodel_isModelLoaded(model: Pointer?): Boolean
    fun llmodel_prompt(
        model: Pointer?,
        prompt: String?,
        promptCallback: LLModelResponseCallback,
        responseCallback: LLModelResponseCallback,
        recalculateCallback: LLModelRecalculateCallback,
        context: LLModelContext?
    )

    interface GPTJ : LLModelLibrary {
        fun llmodel_gptj_create(): Pointer?
        fun llmodel_gptj_destroy(model: Pointer?)
    }

    interface Llama : LLModelLibrary {
        fun llmodel_llama_create(): Pointer?
        fun llmodel_llama_destroy(model: Pointer?)
    }

    fun interface LLModelResponseCallback : Callback {
        fun callback(tokenId: Int, response: String?): Boolean
    }

    fun interface LLModelRecalculateCallback : Callback {
        fun callback(isRecalculating: Boolean): Boolean
    }
}
