package com.xebia.functional.scala.chains.models

import scala.util.control.NoStackTrace

class MissingInputVariablesError(missingVars: Set[String], knownVars: Set[String]) extends Throwable with NoStackTrace:
  override def getMessage(): String = s"Missing required input variables: $missingVars, only had $knownVars"
