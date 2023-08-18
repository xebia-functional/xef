package com.xebia.functional.xef.conversation.expressions

import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.lang.Infer
import kotlinx.serialization.Serializable

enum class Interest {
  History,
  Adventure,
  Relaxation,
  Culture,
  Food,
  Nature,
  Nightlife,
  Shopping,
  Infer
}

@Serializable
data class TravelState(
  val destination: String,
  val interests: List<Interest>,
  val travelDuration: Int // in days
)

@Serializable data class GenerateItinerary(val state: TravelState)

@Serializable
data class ItineraryRecommendationPrompt(
  val destination: String,
  @Description(
    "Generate a travel itinerary based on the destination, interests, and travel duration." +
      "Ensure the itinerary is balanced, considering the user's interests and the time available."
  )
  val dayByDayPlan: List<Plan>,
)

@Serializable data class Plan(val day: String, val activities: List<String>)

suspend fun main() {
  OpenAI.conversation {
    val infer = Infer(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, conversation)
    val itinerary: ItineraryRecommendationPrompt =
      infer(
        Prompt(
          """
                    Assume the role of a seasoned travel expert. Based on the provided destination, interests, and duration, craft a detailed travel itinerary.
                    """
            .trimIndent()
        )
      ) {
        GenerateItinerary(
          state =
            TravelState(
              destination = "Paris",
              interests = listOf(Interest.History, Interest.Food, Interest.Nightlife),
              travelDuration = 3
            )
        )
      }

    println("Travel Itinerary for ${itinerary.dayByDayPlan.size} days in ${itinerary.destination}:")
    itinerary.dayByDayPlan.forEach { (day, activities) ->
      println("\n$day:")
      activities.forEach { activity -> println("- $activity") }
    }
  }
}
