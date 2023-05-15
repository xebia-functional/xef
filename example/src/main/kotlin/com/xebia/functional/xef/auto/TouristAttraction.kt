package com.xebia.functional.xef.auto

import kotlinx.serialization.Serializable

@Serializable
data class TouristAttraction(val name: String, val location: String, val history: String)

suspend fun main() =
    ai {
        val statueOfLiberty: TouristAttraction = prompt("Statue of Liberty location and history.")
        println(
            """|${statueOfLiberty.name} is located in ${statueOfLiberty.location} and has the following history:
                 |${statueOfLiberty.history}""".trimMargin()
        )
    }.getOrElse { println(it) }
