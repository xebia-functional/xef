package com.xebia.functional.scala.chains.models

import scala.util.control.NoStackTrace

class InvalidChainInputsError(inputKeys: Set[String], inputs: Map[String, String]) extends Throwable with NoStackTrace:
  override def getMessage(): String =
    s"The provided inputs (${inputs.keySet.mkString(", ")}) do not match with chain's inputs (${inputKeys.mkString(", ")})"
