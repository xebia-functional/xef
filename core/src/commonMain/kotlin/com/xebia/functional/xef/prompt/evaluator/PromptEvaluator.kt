package com.xebia.functional.xef.prompt.evaluator

import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.models.chat.Message
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

object PromptEvaluator {

  @Serializable data class Score(val scoreCriteria: List<ScoreCriteria>, val reasoning: String)

  @Serializable data class ScoreCriteria(val name: String, val score: Double)

  data class ScoreCriteriaConfig(
    val name: String,
    val functionTitle: String,
    val subfunctions: List<String>,
    val evaluation: String,
  ) {
    companion object {
      val DEFAULTS: List<ScoreCriteriaConfig> =
        listOf(
          ScoreCriteriaConfig(
            "completeness",
            "Completeness",
            listOf("Coverage", "Detail"),
            "thorough"
          ),
          ScoreCriteriaConfig(
            "accuracy",
            "Accuracy",
            listOf("Correctness", "Consistency"),
            "precise"
          ),
          ScoreCriteriaConfig(
            "efficiency",
            "Efficiency",
            listOf("ResponseTime", "ResourceUtilization"),
            "optimized"
          ),
          ScoreCriteriaConfig(
            "robustness",
            "Robustness",
            listOf("NoiseTolerance", "Adaptability"),
            "resilient"
          ),
          ScoreCriteriaConfig(
            "biasAndFairness",
            "BiasAndFairness",
            listOf("DemographicBias", "ContentBias"),
            "unbiased"
          ),
          ScoreCriteriaConfig(
            "interpretability",
            "Interpretability",
            listOf("Clarity", "Explainability"),
            "clear"
          ),
          ScoreCriteriaConfig(
            "usability",
            "Usability",
            listOf("UserExperience", "Accessibility"),
            "user-friendly"
          ),
          ScoreCriteriaConfig(
            "safetyAndSecurity",
            "SafetyAndSecurity",
            listOf("ContentSafety", "DataSecurity"),
            "safe-and-secure"
          ),
          ScoreCriteriaConfig(
            "creativityAndFlexibility",
            "CreativityAndFlexibility",
            listOf("Innovation", "Versatility"),
            "innovative"
          ),
          ScoreCriteriaConfig(
            "internationalization",
            "Internationalization",
            listOf("LanguageSupport", "CulturalSensitivity"),
            "global"
          ),
          ScoreCriteriaConfig(
            "scalability",
            "Scalability",
            listOf("VolumeHandling", "Integration"),
            "scalable"
          ),
          ScoreCriteriaConfig(
            "legalAndEthicalConsiderations",
            "LegalAndEthicalConsiderations",
            listOf("Compliance", "EthicalConsiderations"),
            "compliant"
          ),
        )
    }
  }

  suspend fun evaluate(
    model: Chat,
    conversation: Conversation,
    prompt: String,
    response: String,
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
    scoreConfig: List<ScoreCriteriaConfig> = ScoreCriteriaConfig.DEFAULTS
  ): Score {

    fun printDef(c: ScoreCriteriaConfig): String =
      "            ${c.functionTitle}() {\n${c.subfunctions.map { "              ${it}()" }.joinToString("\n")}\n            }"

    fun printCall(c: ScoreCriteriaConfig): String =
      "                ${c.functionTitle}():evaluation=${c.evaluation}"

    fun printReturn(c: ScoreCriteriaConfig): String = "                ${c.name}: {{infer:double}},"

    val message =
      // language=markdown
      """
          # PromptEvaluator
          
          # Roleplay as an evaluator for testing the performance of prompts over LLMs.
          
          EvaluationProcess {
            State {
              Prompt: String
              Response: String
            }
${scoreConfig.joinToString("\n") { printDef(it) }}
            ScoreRules() {
              Apply a score to each evaluation.
              0 = not met
              0.25 = partially met but not enough
              0.5 = partially met
              0.75 = mostly met
              1 = met
            }
            Evaluate(prompt: String, response: String) : JSON {
              Set Prompt = prompt
              Set Response = response
              ScoreRules() {
${scoreConfig.joinToString("\n") { printCall(it) }}
              }
              return {
${scoreConfig.joinToString("\n") { printReturn(it) }}
                reasoning: {{infer:string;short_answer;max_tokens=50}}
              }
            }:evaluation=complete,returns=json,stringformat=json_escaped
          }
          
          When asked to evaluate a prompt and its response, please carefully follow the
          instructions above and ensure that the evaluation is complete in all aspects. 📝
          Reply exclusively with the evaluation JSON object. 📤
          """
        .trimIndent()

    val result: List<String> =
      model.promptMessages(
        messages =
          listOf(
            Message.systemMessage { message },
            Message.userMessage { "Set Prompt = $prompt" },
            Message.userMessage { "Set Response = $response" },
            Message.userMessage { "Evaluate(Prompt, Response)" }
          ),
        scope = conversation,
        promptConfiguration = promptConfiguration,
      )
    val firstMessage = result.firstOrNull() ?: error("No messages returned from prompt")
    val map =
      Json.decodeFromString(
        MapSerializer(String.serializer(), JsonPrimitive.serializer()),
        firstMessage
      )
    val values: List<ScoreCriteria> =
      map.toList().flatMap {
        val value: Double? = it.second.doubleOrNull
        if (value != null) listOf(ScoreCriteria(it.first, value)) else listOf()
      }
    val reasoning = map["reasoning"]?.toString()
    return Score(values, reasoning.orEmpty())
  }
}
