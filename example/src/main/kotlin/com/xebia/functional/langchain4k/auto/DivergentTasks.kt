package com.xebia.functional.langchain4k.auto

import arrow.core.getOrElse
import com.xebia.functional.auto.AI
import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class NumberOfMedicalNeedlesInWorld(val numberOfNeedles: Long)

suspend fun main() {
    AI {
        val needlesInWorld: NumberOfMedicalNeedlesInWorld = ai(
            """|Provide the number of medical needles in the world.
        """.trimMargin()
        )
        println("Needles in world: ${needlesInWorld.numberOfNeedles}")
    }.getOrElse { println(it) }
}

