package com.xebia.functional.scala.chains.combine

import com.xebia.functional.scala.chains.Chain
import com.xebia.functional.scala.domain.Document

trait CombineDocumentsChain[F[_]] extends Chain[F]:
  def combineDocs(documents: List[Document]): F[Map[String, String]]
