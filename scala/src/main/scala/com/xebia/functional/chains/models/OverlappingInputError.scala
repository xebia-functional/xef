package com.xebia.functional.scala.chains.models

import scala.util.control.NoStackTrace

class OverlappingInputError(overlappingKeys: Set[String]) extends Throwable with NoStackTrace:
  override def getMessage(): String = s"Chain returned keys that already exists $overlappingKeys"
