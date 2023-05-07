package com.xebia.functional.langchain4k.auto

import arrow.core.getOrElse
import com.xebia.functional.auto.ai
import com.xebia.functional.tool.search
import kotlinx.serialization.Serializable

@Serializable
data class NumberOfMedicalNeedlesInWorld(val numberOfNeedles: Long)


suspend fun main() {
    ai {

        agent(*search("Estimate amount of medical needles in the world")) {
            val needlesInWorld: NumberOfMedicalNeedlesInWorld =
                ai("Provide the number of medical needles in the world")
            println("Needles in world: ${needlesInWorld.numberOfNeedles}")
        }

    }.getOrElse { println(it) }
}

