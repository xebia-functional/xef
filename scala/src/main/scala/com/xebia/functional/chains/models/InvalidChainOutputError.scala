package com.xebia.functional.scala.chains.models

import scala.util.control.NoStackTrace

class InvalidChainOutputError(outputKeys: Set[String]) extends Throwable with NoStackTrace:
  override def getMessage(): String = s"The expected outputs are more than one: ${outputKeys.mkString(", ")}"
