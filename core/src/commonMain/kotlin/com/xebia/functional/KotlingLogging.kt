package com.xebia.functional

import io.github.oshai.KLogger

fun KLogger.logTruncated(ctx: String, msg: String, max: Int = 100): Unit = debug {
  if (msg.length > max) "[$ctx] ${msg.take(max)}..." else "[$ctx] $msg"
}
