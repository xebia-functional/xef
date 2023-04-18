package llm.openai

import llm.models.CompletionChoice

actual object OpenAIClientFactory {
    actual fun createClient(): OpenAIClient = JsOpenAIClient
}

object JsOpenAIClient : OpenAIClient {
    override suspend fun createCompletion(request: String): List<CompletionChoice> {
        TODO("Not yet implemented")
    }
}