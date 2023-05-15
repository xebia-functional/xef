package com.xebia.functional.xef.auto

import com.xebia.functional.auto.ai
import com.xebia.functional.auto.getOrElse
import com.xebia.functional.agents.search
import kotlinx.serialization.Serializable

@Serializable
data class NumberOfMedicalNeedlesInWorld(val numberOfNeedles: Long)


suspend fun main() {
    ai {
        context(search("Estimate amount of medical needles in the world")) {
            val needlesInWorld: NumberOfMedicalNeedlesInWorld =
                prompt("Provide the number of medical needles in the world")
            println("Needles in world: ${needlesInWorld.numberOfNeedles}")
        }

    }.getOrElse { println(it) }
}

