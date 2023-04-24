package com.xebia.functional.agents.models

import scala.util.control.NoStackTrace

class UnsupportedEarlyStoppingMethodError(method: String) extends Throwable with NoStackTrace:
  override def getMessage(): String = s"Got unsupported early_stopping_method $method"
