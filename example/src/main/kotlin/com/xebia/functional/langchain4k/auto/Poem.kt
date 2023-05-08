package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.ai
import com.xebia.functional.auto.getOrElse
import kotlinx.serialization.Serializable

@Serializable
data class Poem(val title: String, val content: String)

suspend fun main() =
    ai {
      val poem1: Poem = ai("A short poem about the beauty of nature.")
      val poem2: Poem = ai("A short poem about the power of technology.")
      val poem3: Poem = ai("A short poem about the wisdom of artificial intelligence.")

      val combinedPoemContent = "${poem1.content}\n\n${poem2.content}\n\n${poem3.content}"

      val newPoemPrompt = """
          Write a new poem that combines ideas from the following themes: the beauty of nature, the power of technology, and the wisdom of artificial intelligence. Here are some examples of poems on these themes:
  
          $combinedPoemContent
      """.trimIndent()

      val newPoem: Poem = ai(newPoemPrompt)

      println("New Poem:\n\n${newPoem.content}")
    }.getOrElse { println(it) }
