package com.xebia.functiona.langchain4k.auto

import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class TouristAttraction(val name: String, val location: String, val history: String)

suspend fun main() {
    val statueOfLiberty: TouristAttraction = ai("Statue of Liberty location and history.")
    println(
        """${statueOfLiberty.name} is located in ${statueOfLiberty.location} and has the following history:
|${statueOfLiberty.history}""".trimMargin()
    )
}
