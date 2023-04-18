package llm.openai

import llm.models.CompletionChoice

actual object OpenAIClientFactory {
    actual fun createClient(): OpenAIClient = NativeOpenAIClient
}

object NativeOpenAIClient : OpenAIClient {
    override suspend fun createCompletion(request: String) : List<CompletionChoice> {
        TODO("Not yet implemented")
    }

}