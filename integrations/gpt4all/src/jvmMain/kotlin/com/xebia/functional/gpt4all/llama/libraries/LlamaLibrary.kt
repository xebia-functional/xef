package com.xebia.functional.gpt4all.llama.libraries

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Pointer
import com.sun.jna.Structure

class LlamaContextParams(
    @JvmField
    var n_ctx: Int = 0,
    @JvmField
    var n_parts: Int = 0,
    @JvmField
    var seed: Int = 0,
    @JvmField
    var f16_kv: Boolean = false,
    @JvmField
    var logits_all: Boolean = false,
    @JvmField
    var vocab_only: Boolean = false,
    @JvmField
    var use_mmap: Boolean = false,
    @JvmField
    var use_mlock: Boolean = false,
    @JvmField
    var embedding: Boolean = false,
    @JvmField
    var progress_callback: LlamaProgressCallback? = null,
    @JvmField
    var progress_callback_user_data: Pointer? = null
) : Structure() {

    override fun getFieldOrder(): List<String> =
        listOf(
            "n_ctx",
            "n_parts",
            "seed",
            "f16_kv",
            "logits_all",
            "vocab_only",
            "use_mmap",
            "use_mlock",
            "embedding",
            "progress_callback",
            "progress_callback_user_data"
        )

    interface LlamaProgressCallback : Callback {
        fun callback(progress: Float, ctx: Pointer)
    }
}

interface LlamaLibrary : Library {
    fun llama_n_embd(context: Pointer): Int

    fun llama_get_embeddings(context: Pointer): Pointer

    fun llama_init_from_file(path_model: String, params: LlamaContextParams): Pointer

    fun llama_apply_lora_from_file(
        ctx: Pointer,
        path_lora: String,
        path_base_model: String,
        n_threads: Int
    ): Int

    fun llama_tokenize(
        ctx: Pointer,
        text: Pointer,
        tokens: Pointer,
        n_max_tokens: Int,
        add_bos: Boolean
    ): Int

    fun llama_eval(
        ctx: Pointer,
        tokens: Pointer,
        n_tokens: Int,
        n_past: Int,
        n_threads: Int
    ): Int

    fun llama_free(ctx: Pointer): Unit
}
