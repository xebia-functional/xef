package com.xebia.functional.textsplitters

import com.xebia.functional.domain.Document

trait BaseTextSplitter[F[_]]:
  def splitDocuments(documents: List[Document]): F[List[Document]]
  def splitText(text: String): F[List[String]]
  def splitTextInDocuments(text: String): F[List[Document]]
