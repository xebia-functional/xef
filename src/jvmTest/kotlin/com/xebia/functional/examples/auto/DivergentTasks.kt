package com.xebia.functional.examples.auto

import com.xebia.functional.auto.agents.CurlUnixCommand
import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class NumberOfMedicalNeedlesInWorld(val numberOfNeedles: Long)

suspend fun main() {
    val needlesInWorld: NumberOfMedicalNeedlesInWorld = ai(
        """|Provide the number of medical needles in the world.
        """.trimMargin(),
        agents = listOf(CurlUnixCommand)
    )
    println("Needles in world: ${needlesInWorld.numberOfNeedles}")
}

