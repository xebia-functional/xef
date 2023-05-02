package com.xebia.functional.loaders

import cats.effect.Resource

import com.xebia.functional.domain.Document
import com.xebia.functional.textsplitters.BaseTextSplitter

trait BaseLoader[F[_]]:
  def load: F[List[Document]]
  def loadAndSplit(textSplitter: BaseTextSplitter[F]): F[List[Document]]
