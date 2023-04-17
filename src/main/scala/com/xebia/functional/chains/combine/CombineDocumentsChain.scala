package com.xebia.functional.chains.combine

import com.xebia.functional.chains.BaseChain
import com.xebia.functional.domain.Document

trait CombineDocumentsChain[F[_]] extends BaseChain[F]:
  def combineDocs(documents: List[Document]): F[Map[String, String]]
