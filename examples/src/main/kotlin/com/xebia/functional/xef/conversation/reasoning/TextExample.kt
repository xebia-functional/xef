package com.xebia.functional.xef.conversation.reasoning

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.reasoning.text.Text
import com.xebia.functional.xef.reasoning.text.summarize.SummaryLength

suspend fun main() {
  OpenAI.conversation {
    val text = Text(model = OpenAI.fromEnvironment().DEFAULT_CHAT, scope = this)

    val inputText =
      """
       The recent advancements in artificial intelligence have had a significant impact on various industries. 
       Natural Language Processing (NLP) is one such area that has gained a lot of attention. 
       NLP deals with the interaction between computers and human language, and it plays a crucial role in text analysis and understanding. 
       In this example, we will explore various NLP tasks using the capabilities of the Xebia Xef AI Reasoning Framework.
    """
        .trimIndent()

    val dataAnonymizationResult = text.dataAnonymization(inputText)
    println("Data Anonymization Result:")
    println(dataAnonymizationResult)
    println()

    val argumentMiningResult = text.argumentMining(inputText)
    val stanceDetectionResult = text.stanceDetection(inputText)

    val coreferenceResolutionResult = text.coreferenceResolution(inputText)

    val emotionDetectionResult = text.emotionDetection(inputText)

    val entityRecognitionResult = text.entityRecognition(inputText)

    val eventExtractionResult = text.eventExtraction(inputText)

    val factCheckingResult = text.factChecking(inputText)

    val grammarCorrectionResult = text.grammarCorrection(inputText)

    val intentRecognitionResult = text.intentRecognition(inputText)

    val keywordExtractionResult = text.keywordExtraction(inputText)

    val languageDetectionResult = text.languageDetection(inputText)

    val languageTranslationResult = text.languageTranslation(inputText)

    val relationshipExtractionResult = text.relationshipExtraction(inputText)

    val sentimentAnalysisResult = text.sentimentAnalysis(inputText)

    val summarizeResult =
      text.summarize.summarizeLargeText(text = inputText, summaryLength = SummaryLength.DEFAULT)

    val textSimplificationResult = text.textSimplification(inputText)

    println()
    println("Argument Mining Result:")
    println(argumentMiningResult)
    println()
    println("Stance Detection Result:")
    println(stanceDetectionResult)
    println()
    println("Coreference Resolution Result:")
    println(coreferenceResolutionResult)
    println()
    println("Emotion Detection Result:")
    println(emotionDetectionResult)
    println()
    println("Entity Recognition Result:")
    println(entityRecognitionResult)
    println()
    println("Event Extraction Result:")
    println(eventExtractionResult)
    println()
    println("Fact Checking Result:")
    println(factCheckingResult)
    println()
    println("Grammar Correction Result:")
    println(grammarCorrectionResult)
    println()
    println("Intent Recognition Result:")
    println(intentRecognitionResult)
    println()
    println("Keyword Extraction Result:")
    println(keywordExtractionResult)
    println()
    println("Language Detection Result:")
    println(languageDetectionResult)
    println()
    println("Language Translation Result:")
    println(languageTranslationResult)
    println()
    println("Relationship Extraction Result:")
    println(relationshipExtractionResult)
    println()
    println("Sentiment Analysis Result:")
    println(sentimentAnalysisResult)
    println()
    println("Summarize Result:")
    println(summarizeResult)
    println()
    println("Text Simplification Result:")
    println(textSimplificationResult)
  }
}
