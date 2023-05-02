package com.xebia.functional.chains.retrievalqa

import com.xebia.functional.chains.BaseChain
import com.xebia.functional.domain.Document

trait RetrievalQAChain[F[_]] extends BaseChain[F]:
  def getDocs(question: String): F[List[Document]]
