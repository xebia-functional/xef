package com.xebia.functional.scala.chains.retrievalqa

import com.xebia.functional.scala.chains.BaseChain
import com.xebia.functional.scala.domain.Document

trait RetrievalQAChain[F[_]] extends BaseChain[F]:
  def getDocs(question: String): F[List[Document]]
