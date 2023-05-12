package com.xebia.functional.tools

import io.github.oshai.KLogger

class Agent<out M>(
  val name: String,
  val description: String,
  val action: suspend KLogger.() -> List<M>,
) {
  fun <E> map(transform: (M) -> E): Agent<E> = Agent(name, description) { action().map(transform) }
}
