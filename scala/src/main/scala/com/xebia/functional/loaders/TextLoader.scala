package com.xebia.functional.loaders

import scala.io.Source

import cats.effect.Resource
import cats.effect.Sync
import cats.syntax.all.*

import com.xebia.functional.domain.Document
import com.xebia.functional.textsplitters.BaseTextSplitter

class TextLoader[F[_]: Sync](filePath: String) extends BaseLoader[F]:

  private def readFile(path: String): F[Source] = Sync[F].delay(Source.fromFile(path))
  private def getContent(source: Source): F[List[Document]] = Sync[F].delay(source.getLines().toList.map(Document.apply))
  private def closeFile(source: Source): F[Unit] = Sync[F].delay(source.close())

  override def load: F[List[Document]] =
    Resource
      .make(readFile(filePath))(closeFile)
      .use(getContent)
      .adaptErr { case e: Throwable => LoaderError(e.getMessage) }

  override def loadAndSplit(splitter: BaseTextSplitter[F]): F[List[Document]] =
    load
      .flatMap(splitter.splitDocuments)
      .adaptErr { case e: Throwable => LoaderError(e.getMessage) }
