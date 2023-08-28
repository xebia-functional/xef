package com.xebia.functional.xef.conversation.streaming

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.OpenAIEmbeddings
import com.xebia.functional.xef.llm.StreamedFunction
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.store.LocalVectorStore

/*
 * This examples is the same than SpaceCraft.kt but using a local server
 *
 * To run this example, you need to:
 *  - Execute xef-server in local using the command: ./gradlew server
 */
suspend fun main() {

  val model = OpenAI(host = "http://localhost:8081/").DEFAULT_SERIALIZATION

  val scope =
    Conversation(LocalVectorStore(OpenAIEmbeddings(OpenAI.FromEnvironment.DEFAULT_EMBEDDING)))

  model
    .promptStreaming(
      Prompt("Make a spacecraft with a mission to Mars"),
      scope = scope,
      serializer = InterstellarCraft.serializer()
    )
    .collect { element ->
      when (element) {
        is StreamedFunction.Property -> {
          println("${element.path} = ${element.value}")
        }
        is StreamedFunction.Result -> {
          println(element.value)
        }
      }
    }
}
