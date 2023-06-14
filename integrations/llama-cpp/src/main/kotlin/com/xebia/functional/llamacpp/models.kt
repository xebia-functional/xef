package com.xebia.functional.llamacpp

data class LogitBias(
    val token: Int,
    val bias: Float,
)

data class LlamaGenerationConfig(
    val n_threads: Int = 1,
    val logit_bias: List<LogitBias>? = null,
    val top_k: Int? = 40,
    val top_p: Double? = 0.95,
    val tfs_z: Double? = 1.0,
    val temp: Double? = 0.8,
    val typical_p: Double? = 1.0,
    val repeat_penalty: Double? = 1.1,
    val repeat_last_n: Int? = 64,
    val frequency_penalty: Double? = 0.0,
    val presence_penalty: Double? = 0.0,
    val mirostat: Int? = 0,
    val mirostat_tau: Double? = 5.0,
    val mirostat_eta: Double? = 0.1,
    val stop_sequence: String? = null,
    val penalize_nl: Boolean? = true
)

data class LlamaLoraAdaptor(
    val lora_adapter: String,
    val lora_base: String?,
    val n_threads: Int
)

data class LlamaConfig(
    val model_path: String,
    val n_ctx: Int = 256,
    val n_parts: Int = -1,
    val seed: Int = 1,
    val f16_kv: Boolean = false,
    val logits_all: Boolean = false,
    val vocab_only: Boolean = false,
    val use_mlock: Boolean = false,
    val embedding: Boolean = true,
    val use_mmap: Boolean = true,
    val lora: LlamaLoraAdaptor? = null
)

data class Embedding(
    val embedding: List<Float>
)

data class EmbeddingRequest(
    val input: List<String>
)

data class EmbeddingResponse(
    val model: String,
    val data: List<Embedding>
)
