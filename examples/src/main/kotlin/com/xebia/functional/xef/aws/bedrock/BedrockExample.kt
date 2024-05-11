package com.xebia.functional.xef.aws.bedrock

import com.xebia.functional.xef.aws.bedrock.models.BedrockModel
import com.xebia.functional.xef.conversation.Description
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
@Description("A planet in our solar system.")
data class Planet(
  @Description("The name of the planet.")
  val name: String,
  @Description("The distance of the planet from the sun.")
  val distanceFromSun: String
)

@Serializable
@Description("The sentiment evaluation of a text.")
data class SentimentEvaluation(
  @Required
  @Description("""
    1 if the sentiment is positive,
    0 if the sentiment is neutral,
    -1 if the sentiment is negative.
  """)
  val evaluation: Int
)

/**
 * This is an example of how to use the OpenAI API with the Bedrock runtime.
 * Requires the following environment variables to be set:
 * - AWS_ACCESS_KEY_ID
 * - AWS_SECRET_ACCESS_KEY
 * - AWS_REGION_NAME
 */
suspend fun main() {
  val planet = BedrockModel.Anthropic.claude3<Planet>(
    prompt = "The planet Mars"
  )
  println("planet: $planet")
  val essayStream = BedrockModel.Anthropic.claude3<Flow<String>>(
    prompt = "Write a critique about your less favorite planet: ${planet.name}"
  )
  val essay = StringBuilder()
  essayStream.collect {
    print(it)
    essay.append(it)
  }
  val sentiment = BedrockModel.Anthropic.claude3<SentimentEvaluation>(
    prompt = "$essay\n\nWhat is the sentiment of the essay?"
  )
  println()
  println("sentiment: ${sentiment.evaluation}")

}



