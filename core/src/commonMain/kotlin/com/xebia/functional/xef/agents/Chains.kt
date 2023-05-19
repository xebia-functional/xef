package com.xebia.functional.xef.agents

// from https://docs.langchain.com/docs/components/chains/index_related_chains

// class MapReduceChain<A, B, R>(val mapper: Agent<B>, val reducer: Agent<R>) : Agent<R> {
//  override val name = "MapReduce [${mapper.name}, ${reducer.name}]"
//  override val description: String =
//    "MapReduce [mapper = ${mapper.description}, reducer = ${reducer.description}]"
//
//  override suspend fun Raise<AIError>.call(): R {
//    val mapResults =
//      recover({ mapOrAccumulate(input) { with(mapper) { call() } } }) { e: NonEmptyList<AIError>
//        ->
//        raise(AIError.Combined(e))
//      }
//    return with(reducer) { call() }
//  }
// }
//
// class RefineChain<A, B>(val initial: Agent<A, B>, val refiner: Agent<Pair<A, B>, B>) :
//  Agent<NonEmptyList<A>, B> {
//  override val name = "Refine [${initial.name}, ${refiner.name}]"
//  override val description: String =
//    "Refine [initial = ${initial.description}, refiner = ${refiner.description}]"
//
//  override suspend fun Raise<AIError>.call(): B {
//    val initialResult = with(initial) { call() }
//    return input.tail.fold(initialResult) { acc, x -> with(refiner) { call() } }
//  }
// }
