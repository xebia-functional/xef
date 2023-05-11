package com.xebia.functional.scala.chains.retrievalqa

import com.xebia.functional.scala.chains.Chain
import com.xebia.functional.scala.domain.Document

trait RetrievalQAChain[F[_]] extends Chain[F]:
  def getDocs(question: String): F[List[Document]]
