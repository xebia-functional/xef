package com.xebia.functional.scala.chains.models

import scala.util.control.NoStackTrace

class MissingOutputVariablesError(missingVars: Set[String]) extends Throwable with NoStackTrace:
  override def getMessage(): String = s"Expected output variables that were not found $missingVars"
