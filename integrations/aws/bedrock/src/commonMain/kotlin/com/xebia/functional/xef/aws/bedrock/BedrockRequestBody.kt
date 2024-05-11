package com.xebia.functional.xef.aws.bedrock

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

@Serializable sealed interface BedrockRequestBody

interface BedrockRequestBodyBuilder {
  var prompt: String
}

@Serializable
data class AnthropicRequestBody(
  val prompt: String,
  @SerialName("anthropic_version") val anthropicVersion: String,
  @SerialName("max_tokens_to_sample") val maxTokens: Int,
  @SerialName("stop_sequences") val stopSequences: List<String>,
  val temperature: Double,
  @SerialName("top_k") val topK: Int,
  @SerialName("top_p") val topP: Int
) : BedrockRequestBody {
  class Builder : BedrockRequestBodyBuilder {
    val defaultStopSequences = listOf("\\n\\nHuman:")
    val defaultAnthropicVersion = "bedrock-2023-05-31"
    val defaultMaxTokens = 300
    val defaultTemperature = 0.1
    val defaultTopK = 250
    val defaultTopP = 1
    val defaultPrompt = ""
    override var prompt: String = defaultPrompt
    var anthropicVersion: String = defaultAnthropicVersion
    var maxTokens: Int = defaultMaxTokens
    var stopSequences: List<String> = defaultStopSequences
    var temperature: Double = defaultTemperature
    var topK: Int = defaultTopK
    var topP: Int = defaultTopP

    fun build(): AnthropicRequestBody =
      AnthropicRequestBody(
        prompt,
        anthropicVersion,
        maxTokens,
        stopSequences,
        temperature,
        topK,
        topP
      )
  }
}

@AwsBedrockDsl
inline fun anthropic(block: AnthropicRequestBody.Builder.() -> Unit): JsonElement =
  Json.encodeToJsonElement(AnthropicRequestBody.Builder().apply(block).build())

@Serializable
data class AmazonTitanRequestBody(
  @SerialName("inputText") val prompt: String,
  val textGenerationConfig: TextGenerationConfig
) {
  @Serializable
  data class TextGenerationConfig(
    @SerialName("maxTokenCount") val maxTokens: Int,
    val stopSequences: List<String>,
    val temperature: Double,
    val topP: Double,
  )

  class Builder : BedrockRequestBodyBuilder {
    override var prompt: String = ""
    var maxTokens: Int = 512
    var stopSequences: List<String> = emptyList()
    var temperature: Double = 0.0
    var topP: Double = 0.9

    fun build(): AmazonTitanRequestBody =
      AmazonTitanRequestBody(
        prompt,
        TextGenerationConfig(maxTokens, stopSequences, temperature, topP)
      )
  }
}

@AwsBedrockDsl
inline fun amazonTitan(block: AmazonTitanRequestBody.Builder.() -> Unit): JsonElement =
  Json.encodeToJsonElement(AmazonTitanRequestBody.Builder().apply(block).build())

@Serializable
data class AI21LabsRequestBody(
  @SerialName("inputText") val prompt: String,
  val maxTokens: Int,
  val stopSequences: List<String>,
  val temperature: Double,
  val topP: Double,
  val countPenalty: ScaledValue,
  val presencePenalty: ScaledValue,
  val frequencyPenalty: ScaledValue,
) : BedrockRequestBody {
  @Serializable data class ScaledValue(val scale: Double)

  class Builder : BedrockRequestBodyBuilder {
    override var prompt: String = ""
    var maxTokens: Int = 200
    var stopSequences: List<String> = listOf("##")
    var temperature: Double = 0.7
    var topP: Double = 1.0
    var countPenalty: Double = 0.0
    var presencePenalty: Double = 0.0
    var frequencyPenalty: Double = 0.0

    fun build(): AI21LabsRequestBody =
      AI21LabsRequestBody(
        prompt,
        maxTokens,
        stopSequences,
        temperature,
        topP,
        ScaledValue(countPenalty),
        ScaledValue(presencePenalty),
        ScaledValue(frequencyPenalty)
      )
  }
}

@AwsBedrockDsl
inline fun ai21Labs(block: AmazonTitanRequestBody.Builder.() -> Unit): JsonElement =
  Json.encodeToJsonElement(AmazonTitanRequestBody.Builder().apply(block).build())

@Serializable
data class CohereRequestBody(
  val prompt: String,
  @SerialName("max_tokens") val maxTokens: Int,
  @SerialName("return_likelihoods") val returnLikelihoods: ReturnLikelihoods,
  @SerialName("stop_sequences") val stopSequences: List<String>,
  val temperature: Double,
  @SerialName("p") val topP: Double,
  @SerialName("k") val topK: Double,
) : BedrockRequestBody {
  @Serializable
  enum class ReturnLikelihoods {
    ALL,
    NONE,
    GENERATION
  }

  class Builder : BedrockRequestBodyBuilder {
    override var prompt: String = ""
    var maxTokens: Int = 200
    var returnLikelihoods: ReturnLikelihoods = ReturnLikelihoods.NONE
    var stopSequences: List<String> = listOf("##")
    var temperature: Double = 0.7
    var topK: Double = 1.0
    var topP: Double = 1.0

    fun build(): CohereRequestBody =
      CohereRequestBody(
        prompt,
        maxTokens,
        returnLikelihoods,
        stopSequences,
        temperature,
        topK,
        topP
      )
  }
}

@AwsBedrockDsl
inline fun cohere(block: CohereRequestBody.Builder.() -> Unit): JsonElement =
  Json.encodeToJsonElement(CohereRequestBody.Builder().apply(block).build())

@Serializable
data class StabilityAIRequestBody(
  @SerialName("text_prompts") val textPrompts: List<TextPrompt>,
  @SerialName("cfg_scale") val cfgScale: Int,
  val height: Int,
  val sampler: Sampler?,
  val samples: Int,
  val seed: Int,
  val steps: Int,
  @SerialName("style_preset") val stylePreset: StylePreset?,
  val width: Int,
) : BedrockRequestBody {
  @Serializable
  enum class Sampler {
    DDIM,
    DDPM,
    K_DPMPP_2M,
    K_DPMPP_2S_ANCESTRAL,
    K_DPM_2,
    K_DPM_2_ANCESTRAL,
    K_EULER,
    K_EULER_ANCESTRAL,
    K_HEUN,
    K_LMS
  }

  @Serializable
  enum class StylePreset {
    @SerialName("3d-model") TriDimensionalModel,
    @SerialName("analog-film") AnalogFilm,
    @SerialName("anime") Anime,
    @SerialName("cinematic") Cinematic,
    @SerialName("comic-book") ComicBook,
    @SerialName("digital-art") DigitalArt,
    @SerialName("enhance") Enhance,
    @SerialName("fantasy-art") FantasyArt,
    @SerialName("isometric") Isometric,
    @SerialName("line-art") LineArt,
    @SerialName("low-poly") LowPoly,
    @SerialName("modeling-compound") ModelingCompound,
    @SerialName("neon-punk") NeonPunk,
    @SerialName("origami") Origami,
    @SerialName("photographic") Photographic,
    @SerialName("pixel-art") PixelArt,
    @SerialName("tile-texture") TileTexture
  }

  @Serializable data class TextPrompt(val text: String, val weight: Float)

  class Builder : BedrockRequestBodyBuilder {
    override var prompt: String = ""
    var cfgScale: Int = 7
    var height: Int = 512
    var sampler: Sampler? = null
    var samples: Int = 1
    var seed: Int = 0
    var steps: Int = 30
    var stylePreset: StylePreset? = null
    var width: Int = 512

    fun build(): StabilityAIRequestBody =
      StabilityAIRequestBody(
        listOf(TextPrompt(prompt, 0.5f)),
        cfgScale,
        height,
        sampler,
        samples,
        seed,
        steps,
        stylePreset,
        width
      )
  }
}

@AwsBedrockDsl
inline fun stabilityAI(block: StabilityAIRequestBody.Builder.() -> Unit): JsonElement =
  Json.encodeToJsonElement(StabilityAIRequestBody.Builder().apply(block).build())
