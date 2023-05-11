package com.xebia.functional.scala.chains.combine

import com.xebia.functional.scala.chains.BaseChain
import com.xebia.functional.scala.domain.Document

trait CombineDocumentsChain[F[_]] extends BaseChain[F]:
  def combineDocs(documents: List[Document]): F[Map[String, String]]
