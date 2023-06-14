package com.xebia.functional.gpt4all

data class GenerationConfig(
    val logitsSize: Int = 0,
    val tokensSize: Int = 0,
    val nPast: Int = 0,
    val nCtx: Int = 4096,
    val nPredict: Int = 128,
    val topK: Int = 40,
    val topP: Double = 0.95,
    val temp: Double = 0.28,
    val nBatch: Int = 8,
    val repeatPenalty: Double = 1.1,
    val repeatLastN: Int = 64,
    val contextErase: Double = 0.5
)

data class LlamaLoraAdaptor(
    val lora_adapter: String,
    val lora_base: String?,
    val n_threads: Int
)

data class LlamaConfig(
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

data class Completion(val content: String)

data class Message(val role: Role, val content: String) {
    enum class Role {
        SYSTEM,
        USER,
        ASSISTANT
    }
}

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

data class CompletionRequest(
    val prompt: String,
    val generationConfig: GenerationConfig
)

data class ChatCompletionRequest(
    val messages: List<Message>,
    val generationConfig: GenerationConfig
)

data class CompletionResponse(
    val model: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val choices: List<Completion>
)

data class ChatCompletionResponse(
    val model: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val choices: List<Message>
)
