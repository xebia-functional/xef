package llm.openai

import arrow.core.Either
import llm.models.CompletionChoice

interface OpenAIClient {
    suspend fun createCompletion(request: String): List<CompletionChoice>
}

expect object OpenAIClientFactory {
    fun createClient(): OpenAIClient
}