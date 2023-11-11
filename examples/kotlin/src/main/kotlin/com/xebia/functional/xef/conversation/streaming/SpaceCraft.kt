package com.xebia.functional.xef.conversation.streaming

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.promptStreaming
import com.xebia.functional.xef.llm.StreamedFunction
import com.xebia.functional.xef.metrics.LogsMetric
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.store.LocalVectorStore
import kotlinx.serialization.Serializable

@Serializable
data class MissionDetail(
  @Description("The current speed of the spacecraft") val speed: Int,
  @Description("Destination planets") val destinationPlanets: List<String>,
  @Description("Estimated time of arrival at destination") val eta: String,
  @Description("Mission ID") val missionID: Int
)

@Serializable
data class SpaceTool(
  @Description("The name of the tool") val name: String,
  @Description("The type of the tool") val type: String,
  @Description("The weight of the tool") val weight: Int
)

@Serializable
data class InterstellarCraft(
  @Description("The designation name of the spacecraft") val designation: String,
  @Description("The current mission name") val mission: String,
  @Description("Coordinates of the spacecraft") val coordinates: Pair<Int, Int>,
  @Description("Details related to the mission") val missionDetail: MissionDetail,
  @Description("A list of at least 10 tools that should be in the space craft")
  val tools: List<SpaceTool>
)

suspend fun main() {
  // This example contemplate the case of calling OpenAI directly or
  // calling through a local Xef Server instance.
  // To run the example with the Xef Server, you can execute the following commands:
  //  - # docker compose-up server/docker/postgresql
  //  - # ./gradlew server
  val openAI = OpenAI.fromEnvironment()
  //  val openAI = OpenAI(host = "http://localhost:8081/")
  val model = openAI.DEFAULT_SERIALIZATION

  val scope = Conversation(LocalVectorStore(openAI.DEFAULT_EMBEDDING), LogsMetric())

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
