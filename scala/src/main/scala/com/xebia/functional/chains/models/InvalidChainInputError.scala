package com.xebia.functional.chains.models

import scala.util.control.NoStackTrace

class InvalidChainInputError(inputKeys: Set[String]) extends Throwable with NoStackTrace:
  override def getMessage(): String = s"The expected inputs are more than one: ${inputKeys.mkString(", ")}"
