package com.xebia.functional.xef.agents

import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.mapOrAccumulate
import arrow.core.raise.recover
import com.xebia.functional.xef.AIError

// from https://docs.langchain.com/docs/components/chains/index_related_chains

class MapReduceChain<A, B, R>(val mapper: Agent<A, B>, val reducer: Agent<List<B>, R>) :
  Agent<List<A>, R> {
  override val name = "MapReduce [${mapper.name}, ${reducer.name}]"
  override val description: String =
    "MapReduce [mapper = ${mapper.description}, reducer = ${reducer.description}]"

  override suspend fun Raise<AIError>.call(input: List<A>): R {
    val mapResults =
      recover({ mapOrAccumulate(input) { with(mapper) { call(it) } } }) { e: NonEmptyList<AIError>
        ->
        raise(AIError.Combined(e))
      }
    return with(reducer) { call(mapResults) }
  }
}

class RefineChain<A, B>(val initial: Agent<A, B>, val refiner: Agent<Pair<A, B>, B>) :
  Agent<NonEmptyList<A>, B> {
  override val name = "Refine [${initial.name}, ${refiner.name}]"
  override val description: String =
    "Refine [initial = ${initial.description}, refiner = ${refiner.description}]"

  override suspend fun Raise<AIError>.call(input: NonEmptyList<A>): B {
    val initialResult = with(initial) { call(input.head) }
    return input.tail.fold(initialResult) { acc, x -> with(refiner) { call(Pair(x, acc)) } }
  }
}
