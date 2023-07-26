package com.xebia.functional.xef.reasoning.text

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.reasoning.text.summarize.Summarize
import com.xebia.functional.xef.reasoning.tools.LLMTool
import com.xebia.functional.xef.reasoning.tools.Tool
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

class Text(
  private val model: Chat,
  private val scope: CoreAIScope,
  @JvmField
  val dataAnonymization: LLMTool =
    LLMTool.create(
      name = "DataAnonymization",
      description = "Anonymize data",
      model = model,
      scope = scope
    ),
  @JvmField
  val argumentMining: LLMTool =
    LLMTool.create(
      name = "ArgumentMining",
      description = "Mine arguments",
      model = model,
      scope = scope
    ),
  @JvmField
  val stanceDetection: LLMTool =
    LLMTool.create(
      name = "StanceDetection",
      description = "Detect stance",
      model = model,
      scope = scope
    ),
  @JvmField
  val categoriesSelection: LLMTool =
    LLMTool.create(
      name = "CategoriesSelection",
      description = "Return a list of categories",
      model = model,
      scope = scope,
      instructions =
        listOf(
          "Return a list of categories for the `text`",
        )
    ),
  @JvmField
  val coreferenceResolution: LLMTool =
    LLMTool.create(
      name = "CoreferenceResolution",
      description = "Resolve coreferences",
      model = model,
      scope = scope
    ),
  @JvmField
  val emotionDetection: LLMTool =
    LLMTool.create(
      name = "EmotionDetection",
      description = "Detect emotions",
      model = model,
      scope = scope
    ),
  @JvmField
  val entityRecognition: LLMTool =
    LLMTool.create(
      name = "EntityRecognition",
      description = "Recognize entities",
      model = model,
      scope = scope
    ),
  @JvmField
  val eventExtraction: LLMTool =
    LLMTool.create(
      name = "EventExtraction",
      description = "Extract events",
      model = model,
      scope = scope
    ),
  @JvmField
  val factChecking: LLMTool =
    LLMTool.create(
      name = "FactChecking",
      description = "Check facts",
      model = model,
      scope = scope
    ),
  @JvmField
  val grammarCorrection: LLMTool =
    LLMTool.create(
      name = "GrammarCorrection",
      description = "Correct grammar",
      model = model,
      scope = scope
    ),
  @JvmField
  val intentRecognition: LLMTool =
    LLMTool.create(
      name = "IntentRecognition",
      description = "Recognize intents",
      model = model,
      scope = scope
    ),
  @JvmField
  val keywordExtraction: LLMTool =
    LLMTool.create(
      name = "KeywordExtraction",
      description = "Extract keywords",
      model = model,
      scope = scope
    ),
  @JvmField
  val languageDetection: LLMTool =
    LLMTool.create(
      name = "LanguageDetection",
      description = "Detect language",
      model = model,
      scope = scope
    ),
  @JvmField
  val relationshipExtraction: LLMTool =
    LLMTool.create(
      name = "RelationshipExtraction",
      description = "Extract relationships",
      model = model,
      scope = scope
    ),
  @JvmField
  val semanticRoleLabeling: LLMTool =
    LLMTool.create(
      name = "SemanticRoleLabeling",
      description = "Label semantic roles",
      model = model,
      scope = scope
    ),
  @JvmField
  val topicModeling: LLMTool =
    LLMTool.create(
      name = "TopicModeling",
      description = "Model topics",
      model = model,
      scope = scope
    ),
  @JvmField
  val wordSenseDisambiguation: LLMTool =
    LLMTool.create(
      name = "WordSenseDisambiguation",
      description = "Disambiguate word senses",
      model = model,
      scope = scope
    ),
  @JvmField
  val sentimentAnalysis: LLMTool =
    LLMTool.create(
      name = "SentimentAnalysis",
      description = "Analyze sentiment",
      model = model,
      scope = scope
    ),
  @JvmField val summarize: Summarize = Summarize(model, scope),
  @JvmField
  val textSimplification: LLMTool =
    LLMTool.create(
      name = "TextSimplification",
      description = "Simplify text",
      model = model,
      scope = scope
    ),
) {
  val tools: List<Tool> =
    listOf(
      dataAnonymization,
      argumentMining,
      stanceDetection,
      coreferenceResolution,
      emotionDetection,
      entityRecognition,
      eventExtraction,
      factChecking,
      grammarCorrection,
      intentRecognition,
      keywordExtraction,
      languageDetection,
      languageTranslation("en"),
      relationshipExtraction,
      semanticRoleLabeling,
      topicModeling,
      wordSenseDisambiguation,
      sentimentAnalysis,
      summarize,
      textSimplification,
    )

  fun languageTranslation(target: String): LLMTool =
    LLMTool.create(
      name = "LanguageTransalation",
      description = "Trnaslate to $target",
      model = model,
      scope = scope
    )

  companion object {

    @JvmStatic fun create(model: Chat, scope: CoreAIScope): Text = Text(model, scope)
  }
}
