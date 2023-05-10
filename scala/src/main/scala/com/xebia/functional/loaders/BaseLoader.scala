package com.xebia.functional.scala.loaders

import cats.effect.Resource

import com.xebia.functional.scala.domain.Document
import com.xebia.functional.scala.textsplitters.BaseTextSplitter

trait BaseLoader[F[_]]:
  def load: F[List[Document]]
  def loadAndSplit(textSplitter: BaseTextSplitter[F]): F[List[Document]]
