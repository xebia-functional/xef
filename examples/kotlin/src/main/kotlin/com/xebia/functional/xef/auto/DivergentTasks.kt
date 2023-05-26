package com.xebia.functional.xef.auto

import com.xebia.functional.xef.agents.search
import kotlinx.serialization.Serializable

@Serializable
data class NumberOfMedicalNeedlesInWorld(val numberOfNeedles: Long)


suspend fun main() {
    ai {
        contextScope(search("Estimate amount of medical needles in the world")) {
            val needlesInWorld: NumberOfMedicalNeedlesInWorld =
                prompt("Provide the number of medical needles in the world")
            println("Needles in world: ${needlesInWorld.numberOfNeedles}")
        }

    }.getOrElse { println(it) }
}

