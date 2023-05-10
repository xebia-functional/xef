package com.xebia.functional.scala.textsplitters

import cats.Applicative
import cats.ApplicativeThrow
import cats.syntax.all.*

import com.xebia.functional.scala.domain.Document

class CharacterTextSplitter[F[_]: ApplicativeThrow](separator: String) extends BaseTextSplitter[F]:
  def splitText(text: String): F[List[String]] =
    ApplicativeThrow[F]
      .catchNonFatal(
        text.split(separator).toList
      ).adaptErr(toCharacterSplitterError)

  def splitDocuments(documents: List[Document]): F[List[Document]] =
    ApplicativeThrow[F]
      .catchNonFatal(
        documents.flatMap(doc => doc.content.split(separator).toList.map(Document.apply))
      ).adaptErr(toCharacterSplitterError)

  def splitTextInDocuments(text: String): F[List[Document]] =
    ApplicativeThrow[F]
      .catchNonFatal(
        text.split(separator).toList.map(Document.apply)
      ).adaptErr(toCharacterSplitterError)

  private def toCharacterSplitterError: PartialFunction[Throwable, Throwable] = { case e: Throwable =>
    SplitterError(Option(e.getMessage).getOrElse("<null>"))
  }
