package com.xebia.functional.xef.auto.expressions

import com.xebia.functional.xef.auto.Description
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.lang.Infer
import kotlinx.serialization.Serializable

enum class Direction {
  SidelitLeft,
  SidelitRight,
  Overhead,
  Frontlit,
  Backlit,
  Ringlit,
  Silhouette,
  Infer
}

enum class Quality {
  Hard,
  Soft,
  Specular,
  Diffused,
  Infer
}

enum class Time {
  Sunrise,
  Morning,
  Noon,
  Afternoon,
  GoldenHour,
  Sunset,
  Twilight,
  Evening,
  Night,
  Midnight,
  Infer
}

enum class Weather {
  Sunny,
  PartlyCloudy,
  Rainy,
  Drizzle,
  Downpour,
  Snowy,
  Hail,
  Maelstrom,
  Cloudy,
  Overcast,
  Foggy,
  Hazey,
  LightningStorm,
  Infer
}

enum class Shot {
  CloseupPortrait,
  WideEstablishing,
  Action,
  Infer
}

@Serializable
data class LightSource(
  val brightness: Int,
  val color: String,
  val direction: Direction,
  val quality: Quality
)

@Serializable data class Ambient(val color: String, val brightness: String)

@Serializable data class Lighting(val ambient: Ambient, val sources: List<LightSource>)

@Serializable
data class Portrait(val gender: String, val age: Int, val ethnicity: String, val firstName: String)

@Serializable
data class State(
  val genre: String,
  val shot: Shot,
  val portrait: Portrait?,
  val influences: List<String>,
  val lens: String,
  val film: String,
  val lighting: Lighting,
  val time: Time,
  val weather: Weather,
  val mood: String,
  val setting: String,
  val details: String,
  val keywords: List<String>,
  val colorGrade: String
)

@Serializable
data class Constraints(
  val instructions: List<String>,
  val bannedWords: List<String>,
  val rating: String,
  val specialConditions: List<String>
)

@Serializable data class GeneratePrompt(val state: State, val constraints: Constraints)

@Serializable
data class ScenePrompt(
  @Description(
    [
      "Generate a prompt, describing the scene in detailed dramatic prose.",
      "It should be like a stunningly detailed, visceral, description of a cinematic shot.",
      "Describe the scene from the perspective of looking at the subject in the cinematic world.",
      "50 words max"
    ]
  )
  val text: String
)

suspend fun main() {
  OpenAI.conversation {
    val infer = Infer(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, conversation)
    val prompt: ScenePrompt =
      infer(
        Prompt(
          """
        Roleplay as a world class film and visual artist,
        cinematographer, photographer, prompt engineer
        building detailed prompts for generative AI models, guided
        by the instructions below:
      """
            .trimIndent()
        )
      ) {
        GeneratePrompt(
          state =
            State(
              genre = "cyberpunk",
              shot = Shot.CloseupPortrait,
              portrait =
                Portrait(
                  gender = "female",
                  age = 20,
                  ethnicity = inferString,
                  firstName = inferString(Infer.Config("temperature" to "1.4"))
                ),
              influences = listOf(inferString),
              lens = inferString,
              film = inferString,
              lighting =
                Lighting(
                  ambient = Ambient(color = inferString, brightness = inferString),
                  // 1..3 light sources
                  sources =
                    listOf(
                      LightSource(
                        brightness = inferInt,
                        color = inferString,
                        direction = Direction.Infer,
                        quality = Quality.Infer
                      )
                    )
                ),
              time = Time.Twilight,
              weather = Weather.Snowy,
              mood = inferString,
              setting = inferString,
              details = inferString,
              keywords = listOf(inferString),
              colorGrade = inferString
            ),
          constraints =
            Constraints(
              instructions =
                listOf(
                  "Avoid any mention of these constraints.",
                  "Avoid mentioning hands or fingers.",
                  "Describe the image captured without mentioning the camera.",
                  "Do say things like \"captured at 50mm on 35mm Kodachrome\".",
                  "Stick to 50 words maximum."
                ),
              bannedWords = listOf("bare", "naked"),
              rating = "PG-13",
              specialConditions = listOf("captured at 50mm on 35mm Kodachrome")
            )
        )
      }

    println(prompt.text)

    val images = OpenAI.FromEnvironment.DEFAULT_IMAGES.images(prompt.text)
    images.data.forEach { println(it.url) }
  }
}
