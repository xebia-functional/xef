package com.xebia.functional.xef.reasoning.text

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.reasoning.text.anonymization.DataAnonymization
import com.xebia.functional.xef.reasoning.text.arguments.ArgumentMining
import com.xebia.functional.xef.reasoning.text.arguments.StanceDetection
import com.xebia.functional.xef.reasoning.text.choices.Choose
import com.xebia.functional.xef.reasoning.text.coreference.CoreferenceResolution
import com.xebia.functional.xef.reasoning.text.emotions.EmotionDetection
import com.xebia.functional.xef.reasoning.text.entities.EntityRecognition
import com.xebia.functional.xef.reasoning.text.events.EventExtraction
import com.xebia.functional.xef.reasoning.text.facts.FactChecking
import com.xebia.functional.xef.reasoning.text.grammar.GrammarCorrection
import com.xebia.functional.xef.reasoning.text.intents.IntentRecognition
import com.xebia.functional.xef.reasoning.text.keywords.KeywordExtraction
import com.xebia.functional.xef.reasoning.text.language.LanguageDetection
import com.xebia.functional.xef.reasoning.text.language.LanguageTranslation
import com.xebia.functional.xef.reasoning.text.relationships.RelationshipExtraction
import com.xebia.functional.xef.reasoning.text.semantics.*
import com.xebia.functional.xef.reasoning.text.sentiments.SentimentAnalysis
import com.xebia.functional.xef.reasoning.text.summarize.Summarize
import com.xebia.functional.xef.reasoning.text.summarize.TextSimplification
import kotlin.jvm.JvmField

class Text(
  private val chatModel: Chat,
  private val serializationModel: ChatWithFunctions,
  private val scope: CoreAIScope,
  @JvmField val dataAnonymization: DataAnonymization = DataAnonymization(serializationModel, scope),
  @JvmField val argumentMining: ArgumentMining = ArgumentMining(serializationModel, scope),
  @JvmField val stanceDetection: StanceDetection = StanceDetection(serializationModel, scope),
  @JvmField val choose: Choose = Choose(serializationModel, scope),
  @JvmField
  val coreferenceResolution: CoreferenceResolution =
    CoreferenceResolution(serializationModel, scope),
  @JvmField val emotionDetection: EmotionDetection = EmotionDetection(serializationModel, scope),
  @JvmField val entityRecognition: EntityRecognition = EntityRecognition(serializationModel, scope),
  @JvmField val eventExtraction: EventExtraction = EventExtraction(serializationModel, scope),
  @JvmField val factChecking: FactChecking = FactChecking(serializationModel, scope),
  @JvmField val grammarCorrection: GrammarCorrection = GrammarCorrection(serializationModel, scope),
  @JvmField val intentRecognition: IntentRecognition = IntentRecognition(serializationModel, scope),
  @JvmField val keywordExtraction: KeywordExtraction = KeywordExtraction(serializationModel, scope),
  @JvmField val languageDetection: LanguageDetection = LanguageDetection(serializationModel, scope),
  @JvmField
  val languageTranslation: LanguageTranslation = LanguageTranslation(serializationModel, scope),
  @JvmField
  val relationshipExtraction: RelationshipExtraction =
    RelationshipExtraction(serializationModel, scope),
  @JvmField
  val semanticRoleLabeling: SemanticRoleLabeling = SemanticRoleLabeling(serializationModel, scope),
  @JvmField val textualEntailment: TextualEntailment = TextualEntailment(serializationModel, scope),
  @JvmField val topicModeling: TopicModeling = TopicModeling(serializationModel, scope),
  @JvmField
  val wordSenseDisambiguation: WordSenseDisambiguation =
    WordSenseDisambiguation(serializationModel, scope),
  @JvmField val sentimentAnalysis: SentimentAnalysis = SentimentAnalysis(serializationModel, scope),
  @JvmField val summarize: Summarize = Summarize(chatModel, scope),
  @JvmField
  val textSimplification: TextSimplification = TextSimplification(serializationModel, scope),
) {}
