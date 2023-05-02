package com.xebia.functional.chains.models

import scala.util.control.NoStackTrace

class InvalidCombineDocumentsChainError(chainType: String) extends Throwable with NoStackTrace:
  override def getMessage(): String = s"'$chainType' is not a valid ComineDocumentsChain"
