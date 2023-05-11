package com.xebia.functional.chains

import arrow.core.Either
import arrow.core.right
import com.xebia.functional.AIError
import com.xebia.functional.persistence.Persistence
import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging

interface Chain<in I, out M> {
  suspend fun call(input: I): Either<AIError.Chain, M>
}

class Agent<out M>(
  val name: String,
  val description: String,
  val action: suspend KLogger.() -> List<M>,
): Chain<Unit, List<M>> {
  val logger: KLogger by lazy { KotlinLogging.logger(name) }

  override suspend fun call(input: Unit): Either<AIError.Chain, List<M>> =
    action(logger).right()

  suspend fun storeResults(persistence: Persistence<M, *>) {
    logger.debug { "[${name}] Running" }
    val docs = action(logger)
    if (docs.isNotEmpty()) {
      persistence.addDocuments(docs)
      logger.debug { "[${name}] Found and memorized ${docs.size} docs" }
    } else {
      logger.debug { "[${name}] Found no docs" }
    }
  }

  fun <E> map(transform: (M) -> E): Agent<E> =
    Agent(name, description) { action().map(transform) }
}

suspend fun <M> Array<out Agent<M>>.storeResults(persistence: Persistence<M, *>) = forEach {
  it.storeResults(persistence)
}
