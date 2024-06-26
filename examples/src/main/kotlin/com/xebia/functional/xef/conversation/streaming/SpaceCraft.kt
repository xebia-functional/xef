package com.xebia.functional.xef.conversation.streaming

import com.xebia.functional.xef.AI
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.llm.StreamedFunction
import kotlinx.coroutines.flow.Flow
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
  @Description("The weight of the tool") val weight: Double
)

@Serializable
data class InterstellarCraft(
  @Description("The designation name of the spacecraft") val designation: String,
  @Description("The current mission name") val mission: String,
  @Description("Coordinates of the spacecraft") val coordinates: Pair<Double, Double>,
  @Description("Details related to the mission") val missionDetail: MissionDetail,
  @Description("A list of at least 10 tools that should be in the space craft")
  val tools: List<SpaceTool>
)

suspend fun main() {
  AI<Flow<StreamedFunction<InterstellarCraft>>>(
      prompt = "Make a spacecraft with a mission to Mars",
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
