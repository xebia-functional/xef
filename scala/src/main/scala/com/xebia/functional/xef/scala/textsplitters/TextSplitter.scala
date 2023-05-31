package com.xebia.functional.xef.scala.textsplitters

import com.xebia.functional.loom.LoomAdapter
import com.xebia.functional.xef.textsplitters.TextSplitter as KtTextSplitter
import com.xebia.functional.xef.textsplitters.{CharacterTextSplitterKt, TokenTextSplitterKt}
import com.xebia.functional.tokenizer.ModelType

import scala.jdk.CollectionConverters.*

abstract class TextSplitter private (kt: KtTextSplitter):
  def core: KtTextSplitter = kt
  def splitText(text: String): List[String]
  def splitDocuments(documents: List[String]): List[String]
  def splitTextInDocuments(text: String): List[String]

object TextSplitter:
  private type Ret = java.util.List[String]
  def characterTextSplitter(separator: String): TextSplitter =
    val kt = CharacterTextSplitterKt.CharacterTextSplitter(separator)
    new TextSplitter(kt):
      def splitText(text: String): List[String] =
        LoomAdapter.apply[Ret](kt.splitText(text, _)).asScala.toList
      def splitDocuments(documents: List[String]): List[String] =
        LoomAdapter.apply[Ret](kt.splitDocuments(documents.asJava, _)).asScala.toList
      def splitTextInDocuments(text: String): List[String] =
        LoomAdapter.apply[Ret](kt.splitTextInDocuments(text, _)).asScala.toList

  def tokenTextSplitter(modelType: ModelType, chunkSize: Int, chunkOverlap: Int): TextSplitter =
    val kt = TokenTextSplitterKt.TokenTextSplitter(modelType, chunkSize, chunkOverlap)
    new TextSplitter(kt):
      override def splitText(text: String): List[String] =
        LoomAdapter.apply[Ret](kt.splitText(text, _)).asScala.toList
      override def splitDocuments(documents: List[String]): List[String] =
        LoomAdapter.apply[Ret](kt.splitDocuments(documents.asJava, _)).asScala.toList
      override def splitTextInDocuments(text: String): List[String] =
        LoomAdapter.apply[Ret](kt.splitTextInDocuments(text, _)).asScala.toList
