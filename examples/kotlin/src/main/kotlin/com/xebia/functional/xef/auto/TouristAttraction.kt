package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.llm.openai.conversation
import com.xebia.functional.xef.auto.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable
data class TouristAttraction(val name: String, val location: String, val history: String)

suspend fun main() = conversation {
  val statueOfLiberty: TouristAttraction = prompt("Statue of Liberty location and history.")
  println(
    """|${statueOfLiberty.name} is located in ${statueOfLiberty.location} and has the following history:
                 |${statueOfLiberty.history}"""
      .trimMargin()
  )
}
