package com.xebia.functional.xef.scala.textsplitters

import com.xebia.functional.loom.LoomAdapter
import com.xebia.functional.xef.textsplitters.TextSplitter as KtTextSplitter
import com.xebia.functional.xef.textsplitters.{CharacterTextSplitterKt, TokenTextSplitterKt}
import com.xebia.functional.tokenizer.ModelType
import kotlin.coroutines.Continuation

import scala.jdk.CollectionConverters.*

trait TextSplitter:
  private type Ret = java.util.List[String]
  def core: KtTextSplitter
  def splitText(text: String): List[String] =
    LoomAdapter.apply[Ret](core.splitText(text, _)).asScala.toList
  def splitDocuments(documents: List[String]): List[String] =
    LoomAdapter.apply[Ret](core.splitDocuments(documents.asJava, _)).asScala.toList
  def splitTextInDocuments(text: String): List[String] =
    LoomAdapter.apply[Ret](core.splitTextInDocuments(text, _)).asScala.toList

object TextSplitter:
  def characterTextSplitter(separator: String): TextSplitter = new TextSplitter:
    def core: KtTextSplitter = CharacterTextSplitterKt.CharacterTextSplitter(separator)

  def tokenTextSplitter(modelType: ModelType, chunkSize: Int, chunkOverlap: Int): TextSplitter = new TextSplitter:
    def core: KtTextSplitter = TokenTextSplitterKt.TokenTextSplitter(modelType, chunkSize, chunkOverlap)
