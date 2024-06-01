package ai.xef

import ai.xef.langchain4j.KotlinAIServices
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import dev.langchain4j.model.output.structured.Description as Langchain4jDescription
import dev.langchain4j.agent.tool.Tool as Langchain4jTool

typealias Description = Langchain4jDescription
typealias Tool = Langchain4jTool

class AI {

  companion object {
    inline operator fun <reified A: Any> invoke(
      type: KType = typeOf<A>(),
      model: ChatLanguageModel = OpenAiChatModel.withApiKey(System.getenv("OPENAI_API_KEY")),
      tools: List<Any> = emptyList(),
      streamingModel: StreamingChatLanguageModel = OpenAiStreamingChatModel.withApiKey(System.getenv("OPENAI_API_KEY")),
      memory: MessageWindowChatMemory = MessageWindowChatMemory.withMaxMessages(10),
    ): A =
      KotlinAIServices<A>(type)
        .chatLanguageModel(model)
        .streamingChatLanguageModel(streamingModel)
        .tools(tools)
        .chatMemory(memory)
        .build()
  }

}
