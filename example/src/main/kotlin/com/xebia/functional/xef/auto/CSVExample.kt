package com.xebia.functional.xef.auto.csv

import com.xebia.functional.xef.agents.csv
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.getOrThrow
import okio.Path
import okio.Path.Companion.toPath
import java.io.File
import java.net.URL

suspend fun main(): Unit = ai {
  val resource: URL = javaClass.getResource("/documents/weather.csv") ?: error("Resource not found")
  val path: Path = File(resource.file).path.toPath()
  contextScope(csv(path)) {
    val result = promptMessage("What is this content about?")
    println(result)
  }
}.getOrThrow()
