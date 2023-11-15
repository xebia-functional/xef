package com.xebia.functional.xef.conversation.expressions

import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.lang.Infer
import kotlinx.serialization.Serializable

enum class DigitalActivity {
  SocialMedia,
  Work,
  Entertainment,
  Gaming,
  News,
  Browsing,
  Infer
}

@Serializable data class DigitalHabits(val activity: DigitalActivity, val dailyHours: Float)

@Serializable
data class DetoxState(
  val currentScreenTime: Float, // in hours
  val primaryActivities: List<DigitalHabits>,
  val detoxGoal: String // e.g., "Reduce Social Media consumption by 50%"
)

@Serializable data class GenerateDetoxPlan(val state: DetoxState)

@Serializable
data class DetoxRecommendationPrompt(
  @Description(
    "Craft a digital detox plan based on the user's current habits and desired goals." +
      "Recommend actionable steps and alternative non-digital activities to aid the detox process."
  )
  val dayByDayActions:
    List<DetoxPlan>, // e.g., {"Day 1": ["1-hour nature walk", "Read a book for 30 minutes"]}
  val summary: String
)

@Serializable data class DetoxPlan(val day: String, val actions: List<String>)

suspend fun main() {
  OpenAI.conversation {
    val infer = Infer(OpenAI.fromEnvironment().DEFAULT_SERIALIZATION, conversation)
    val detoxPlan: DetoxRecommendationPrompt =
      infer(
        Prompt(
          """
                    Assume the role of a digital wellbeing coach. Based on the user's digital habits and detox goals, suggest a holistic detox plan.
                    """
            .trimIndent()
        )
      ) {
        GenerateDetoxPlan(
          state =
            DetoxState(
              currentScreenTime = 6.0f,
              primaryActivities =
                listOf(
                  DigitalHabits(DigitalActivity.SocialMedia, 3.0f),
                  DigitalHabits(DigitalActivity.Work, 2.0f),
                  DigitalHabits(DigitalActivity.Entertainment, 1.0f)
                ),
              detoxGoal = "Reduce Social Media consumption by 50%"
            )
        )
      }

    println("Digital Detox Plan:")
    detoxPlan.dayByDayActions.forEach { (day, actions) ->
      println("\n$day:")
      actions.forEach { action -> println("- $action") }
    }
    println("\nSummary: ${detoxPlan.summary}")
  }
}
