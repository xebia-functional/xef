package com.xebia.functional.xef.dsl

import com.xebia.functional.xef.AI
import kotlinx.coroutines.flow.Flow

suspend fun main() {
  val result = AI<Flow<String>>("List of planets in the solar system")
  result.collect(::print)
}
