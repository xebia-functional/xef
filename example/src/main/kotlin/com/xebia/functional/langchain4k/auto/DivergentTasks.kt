package com.xebia.functional.langchain4k.auto

import com.xebia.functional.auto.ai
import com.xebia.functional.auto.agents.Agent
import com.xebia.functional.auto.agents.wikipedia
import kotlinx.serialization.Serializable

@Serializable
data class NumberOfMedicalNeedlesInWorld(val numberOfNeedles: Long)

suspend fun main() {
    val needlesInWorld: NumberOfMedicalNeedlesInWorld = ai(
        """|Provide the number of medical needles in the world.
        """.trimMargin(),
        agents = listOf(Agent.wikipedia())
    )
    println("Needles in world: ${needlesInWorld.numberOfNeedles}")
}

