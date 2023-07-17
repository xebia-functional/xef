package com.xebia.functional.xef.auto.reasoning

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.OpenAIEmbeddings
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.reasoning.text.Text
import com.xebia.functional.xef.reasoning.text.choices.Choice

suspend fun main() {
  val scope = CoreAIScope(OpenAIEmbeddings(OpenAI.DEFAULT_EMBEDDING))
  val text = Text(chatModel = OpenAI.DEFAULT_CHAT, serializationModel = OpenAI.DEFAULT_SERIALIZATION, scope = scope)

  val inputText = """
       The recent advancements in artificial intelligence have had a significant impact on various industries. 
       Natural Language Processing (NLP) is one such area that has gained a lot of attention. 
       NLP deals with the interaction between computers and human language, and it plays a crucial role in text analysis and understanding. 
       In this example, we will explore various NLP tasks using the capabilities of the Xebia Xef AI Reasoning Framework.
    """.trimIndent()

  val dataAnonymizationResult = text.dataAnonymization.anonymizeText(inputText)
  println("Data Anonymization Result:")
  println(dataAnonymizationResult.anonymizedText)
  println()

  val argumentMiningResult = text.argumentMining.mineArguments(inputText)
  println("Argument Mining Result:")
  argumentMiningResult.arguments.forEach { println(it) }
  println()

  val stanceDetectionResult =
    text.stanceDetection.detectStance(inputText, "The author likes Xebia's Xef AI Reasoning Framework")
  println("Stance Detection Result:")
  println(stanceDetectionResult.stance)
  println()

  val chooseResult = text.choose.chooseBestOf(
    Prompt(inputText),
    listOf(Choice("Option 1"), Choice("Option 2"))
  )
  println("Choice Result:")
  println(chooseResult.choice)
  println()

  val coreferenceResolutionResult = text.coreferenceResolution.resolveCoreferences(inputText)
  println("Coreference Resolution Result:")
  println(coreferenceResolutionResult.coreferences)
  println()

  val emotionDetectionResult = text.emotionDetection.detectEmotion(inputText)
  println("Emotion Detection Result:")
  println(emotionDetectionResult.emotion)
  println()

  val entityRecognitionResult = text.entityRecognition.recognizeEntities(
    inputText,
    listOf("context", "location", "organization", "person", "product", "time", "company")
  )
  println("Entity Recognition Result:")
  entityRecognitionResult.results.forEach { println(it) }
  println()

  val eventExtractionResult = text.eventExtraction.extractEvents(inputText)
  println("Event Extraction Result:")
  eventExtractionResult.events.forEach { println(it) }
  println()

  val factCheckingResult = text.factChecking.factCheck(
    statement = inputText,
    knownFacts = "Xef reasoning framework is cool!"
  )
  println("Fact Checking Result:")
  println(factCheckingResult.result)
  println()

  val grammarCorrectionResult = text.grammarCorrection.correctGrammar(inputText)
  println("Grammar Correction Result:")
  println(grammarCorrectionResult.correctedText)
  println()

  val intentRecognitionResult = text.intentRecognition.recognizeIntent(inputText)
  println("Intent Recognition Result:")
  println(intentRecognitionResult.intent)
  println()

  val keywordExtractionResult = text.keywordExtraction.extractKeywords(inputText)
  println("Keyword Extraction Result:")
  keywordExtractionResult.keywords.forEach { println(it) }
  println()

  val languageDetectionResult = text.languageDetection.identifyLanguage(inputText)
  println("Language Detection Result:")
  println(languageDetectionResult.language)
  println()

  val languageTranslationResult = text.languageTranslation.translateText(inputText, "en")
  println("Language Translation Result:")
  println(languageTranslationResult.translation)
  println()

  val relationshipExtractionResult = text.relationshipExtraction.extractRelationships(inputText)
  println("Relationship Extraction Result:")
  relationshipExtractionResult.relationships.forEach { println(it) }
  println()

  val sentimentAnalysisResult = text.sentimentAnalysis.analyzeSentiment(inputText)
  println("Sentiment Analysis Result:")
  println(sentimentAnalysisResult.sentiment)
  println()

  val summarizeResult = text.summarize.summarizeLargeText(
    query = "summarize in neutral tone",
    text = inputText,
    summaryLength = 50
  )
  println("Text Summarization Result:")
  println(summarizeResult)
  println()

  val textSimplificationResult = text.textSimplification.simplifyText(inputText)
  println("Text Simplification Result:")
  println(textSimplificationResult.simplifiedText)
  println()
}
