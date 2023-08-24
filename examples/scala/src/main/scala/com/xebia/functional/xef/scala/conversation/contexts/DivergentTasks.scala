package com.xebia.functional.xef.scala.auto.contexts

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.reasoning.serpapi.Search
import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

private final case class NumberOfMedicalNeedlesInWorld(numberOfNeedles: Long) derives SerialDescriptor, Decoder

@main def runDivergentTasks: Unit =
  conversation {
    val search = Search(OpenAI.FromEnvironment.DEFAULT_CHAT, summon[ScalaConversation], 3)
    addContext(search.search("Estimate amount of medical needles in the world").get())
    val needlesInWorld = prompt[NumberOfMedicalNeedlesInWorld](Prompt("Provide the number of medical needles in the world as an integer number"))
    println(s"Needles in world: ${needlesInWorld.numberOfNeedles}")
  }
