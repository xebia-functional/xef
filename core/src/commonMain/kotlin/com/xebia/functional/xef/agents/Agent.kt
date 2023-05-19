package com.xebia.functional.xef.agents

import arrow.core.raise.Raise
import com.xebia.functional.xef.AIError

interface Agent<out Output> {
  val name: String
  val description: String
  suspend fun Raise<AIError>.call(): Output

  fun <B> map(transform: (Output) -> B): Agent<B> = Mapped(this, transform)

  //  /** Record an [input] but don't execute the agent yet. */
  //  fun with(): Agent<Output> =
  //    Agent(name = this.name, description = this.description) {
  //      with(this@Agent) { call() }
  //    }

  private class Mapped<C, out D>(val agent: Agent<C>, val transform: (C) -> D) : Agent<D> {
    override val name = agent.name
    override val description: String = agent.description
    override suspend fun Raise<AIError>.call(): D {
      val o = with(agent) { call() }
      return transform(o)
    }
  }

  companion object {
    operator fun <Output> invoke(
      name: String,
      description: String,
      action: suspend Raise<AIError>.() -> Output
    ): Agent<Output> =
      object : Agent<Output> {
        override val name: String = name
        override val description: String = description
        override suspend fun Raise<AIError>.call(): Output = action()
      }
  }
}

// interface ParameterlessAgent<out Output> : Agent<Output> {
//  override suspend fun Raise<AIError>.call(): Output = call()
//
//  companion object {
//    operator fun <Output> invoke(
//      name: String,
//      description: String,
//      action: suspend Raise<AIError>.() -> Output
//    ): ParameterlessAgent<Output> =
//      object : ParameterlessAgent<Output> {
//        override val name: String = name
//        override val description: String = description
//        override suspend fun Raise<AIError>.call(): Output = action()
//      }
//  }
// }

typealias ContextualAgent = Agent<List<String>>
