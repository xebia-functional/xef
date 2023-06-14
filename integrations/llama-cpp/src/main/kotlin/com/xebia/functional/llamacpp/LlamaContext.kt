package com.xebia.functional.llamacpp

import com.sun.jna.Pointer
import com.xebia.functional.llamacpp.libraries.LlamaContextParams
import com.xebia.functional.llamacpp.libraries.LlamaLibrary

interface LlamaContext {
    val pointer: Pointer

    companion object {
        operator fun invoke(llamaLibrary: LlamaLibrary, params: LlamaConfig): LlamaContext {
            val loraParams: LlamaLoraAdaptor? = params.lora
            val modelPath: String = params.model_path

            val ctx: Pointer = llamaLibrary.llama_init_from_file(modelPath, params.toLlamaContextParams())
            if (loraParams != null) {
                val loraBasePath: String = loraParams.lora_base.orEmpty()
                val loraAdapter: String = loraParams.lora_adapter
                val nThreads: Int = loraParams.n_threads

                llamaLibrary.llama_apply_lora_from_file(
                    ctx,
                    loraAdapter,
                    loraBasePath,
                    nThreads
                )
            }
            return object : LlamaContext {
                override val pointer: Pointer = ctx
            }
        }
    }
}

private fun LlamaConfig.toLlamaContextParams(): LlamaContextParams =
    LlamaContextParams(
        n_ctx = n_ctx,
        seed = seed,
        f16_kv = f16_kv,
        logits_all = logits_all,
        vocab_only = vocab_only,
        use_mmap = use_mmap,
        use_mlock = use_mlock,
        embedding = embedding,
        progress_callback = null,
        progress_callback_user_data = null
    )
