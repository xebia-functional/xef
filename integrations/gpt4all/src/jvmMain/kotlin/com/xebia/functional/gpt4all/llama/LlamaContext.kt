package com.xebia.functional.gpt4all.llama

import com.sun.jna.Pointer
import com.xebia.functional.gpt4all.llama.libraries.LlamaContextParams
import com.xebia.functional.gpt4all.llama.libraries.LlamaLibrary

data class LlamaLoraAdaptor(
    val lora_adapter: String,
    val lora_base: String?,
    val n_threads: Int
)

data class ModelLoad(
    val model_path: String,
    val n_ctx: Int = 2048,
    val n_gpu_layers: Int = 0,
    val seed: Int = 0,
    val f16_kv: Boolean = true,
    val logits_all: Boolean = false,
    val vocab_only: Boolean = false,
    val use_mlock: Boolean = false,
    val embedding: Boolean = false,
    val use_mmap: Boolean = true,
    val lora: LlamaLoraAdaptor? = null
)

interface LlamaContext {
    val pointer: Pointer

    companion object {
        operator fun invoke(llamaLibrary: LlamaLibrary, params: ModelLoad): LlamaContext {
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

private fun ModelLoad.toLlamaContextParams(): LlamaContextParams =
    LlamaContextParams(
        n_ctx = n_ctx,
        n_parts = n_gpu_layers,
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
