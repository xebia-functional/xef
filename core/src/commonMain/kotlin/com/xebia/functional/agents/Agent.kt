package com.xebia.functional.agents

import arrow.core.raise.Raise
import com.xebia.functional.AIError

interface Agent<in Input, out Output> {
    val name: String
    val description: String
    suspend fun Raise<AIError>.call(input: Input): Output

    fun <A> contramap(pretransform: (A) -> Input): Agent<A, Output> =
        Wrapper(pretransform, this) { it }

    fun <B> map(transform: (Output) -> B): Agent<Input, B> =
        Wrapper({ it }, this, transform)

    /**
     * Record an [input] but don't execute the agent yet.
     */
    fun with(input: Input): ParameterlessAgent<Output> =
        ParameterlessAgent<Output>(
            name = this.name,
            description = this.description
        ) {
            with(this@Agent) { call(input) }
        }

    class Wrapper<in A, B, C, out D>(
        val pretransform: (A) -> B,
        val agent: Agent<B, C>,
        val transform: (C) -> D
    ): Agent<A, D> {
        override val name = agent.name
        override val description: String = agent.description
        override suspend fun Raise<AIError>.call(input: A): D {
            val i = pretransform(input)
            val o  = with(agent) { call(i) }
            return transform(o)
        }
    }

    companion object {
        operator fun <Input, Output> invoke(
            name: String,
            description: String,
            action: suspend Raise<AIError>.(Input) -> Output
        ): Agent<Input, Output> = object: Agent<Input, Output> {
            override val name: String = name
            override val description: String = description
            override suspend fun Raise<AIError>.call(input: Input): Output = action(input)
        }
    }
}

interface ParameterlessAgent<out Output>: Agent<Unit, Output> {
    suspend fun Raise<AIError>.call(): Output
    override suspend fun Raise<AIError>.call(input: Unit): Output = call()

    companion object {
        operator fun <Output> invoke(
            name: String,
            description: String,
            action: suspend Raise<AIError>.() -> Output
        ): ParameterlessAgent<Output> = object: ParameterlessAgent<Output> {
            override val name: String = name
            override val description: String = description
            override suspend fun Raise<AIError>.call(): Output = action()
        }
    }
}

typealias ContextualAgent = ParameterlessAgent<List<String>>
