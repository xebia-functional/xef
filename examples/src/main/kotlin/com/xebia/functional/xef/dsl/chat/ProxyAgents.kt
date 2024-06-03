package com.xebia.functional.xef.dsl.chat

import com.xebia.functional.xef.agents.Agents
import com.xebia.functional.xef.agents.System
import com.xebia.functional.xef.agents.Tool
import com.xebia.functional.xef.agents.User
import com.xebia.functional.xef.conversation.Conversation
import kotlinx.serialization.Serializable

/**
 * Tool functions must have a body that returns a serializable object.
 * The input arguments are filled in by an LLM model.
 */
interface AstronomyTools {
  @Tool(
    description = "Get details about the planet: {{planet}}"
  )
  suspend fun getPlanetDetails(planet: String): String =
    """
      Bulleted list of details about the planet: $planet
      - Jupiter is the fifth planet from the Sun and the largest in the Solar System.
      - It is a gas giant with a mass one-thousandth that of the Sun, but two-and-a-half times that of all the other planets in the Solar System combined.
      - Jupiter is one of the brightest objects visible to the naked eye in the night sky, and has been known to ancient civilizations since before recorded history.
      - It is named after the Roman god Jupiter. When viewed from Earth, Jupiter can be bright enough for its reflected light to cast shadows, and is on average the third-brightest natural object in the night sky after the Moon and Venus.
      - Jupiter is primarily composed of hydrogen with a quarter of its mass being helium, though helium comprises only about a tenth of the number of molecules. It may also have a rocky core of heavier elements, but like the other giant planets, Jupiter lacks a well-defined solid surface.
      - Because of its rapid rotation, Jupiter's shape is that of an oblate spheroid (it has a slight but noticeable bulge around the equator). The outer atmosphere is visibly segregated into several bands at different latitudes, resulting in turbulence and storms along their interacting boundaries. A prominent result is the Great Red Spot, a giant storm that is known to have existed since at least the 17th century when it was first seen by telescope.
      - Surrounding Jupiter is a faint planetary ring system and a powerful magnetosphere. Jupiter has 79 known moons, including the four large Galilean moons discovered by Galileo Galilei in 1610. Ganymede, the largest of these, has a diameter greater than that of the planet Mercury.
      - Jupiter has been explored on several occasions by robotic spacecraft, most notably during the early Pioneer and Voyager flyby missions and later by the Galileo orbiter. In late February 2007, Jupiter was visited by the New Horizons probe, which used Jupiter's gravity to increase its speed and bend its trajectory en route to Pluto. The latest probe to visit the planet is Juno, which entered into orbit around Jupiter on July 4, 2016. Future targets for exploration in the Jupiter system include the probable ice-covered liquid ocean of its moon Europa.
      - Jupiter is the only planet in the Solar System whose barycenter with the Sun lies outside the volume of the Sun, though by only 7% of the Sun's radius.
      - The average distance between Jupiter and the Sun is 778 million km (about 5.2 times the average distance between Earth and the Sun, or 5.2 AU) and it completes an orbit every 11.86 years.
    """.trimIndent()
}

/**
 * @System annotation is used to provide instructions to the AI model for the
 * AI agent proxy that auto implements the Astronomer interface.
 */
@System(
  instructions = [
    "You are an expert astronomer",
  ],
  tools = [
    AstronomyTools::class
  ]
)
@Serializable
sealed interface Astronomer {
  suspend fun getPlanetInfo(
    @User(
      prompt = ["Tell me info about my favorite planet: {{planet}}"],
      tools = [AstronomyTools::class]
    )
    planet: String
  ): String

  @Tool(
    description = "Search for information about the planet: {{planet}}"
  )
  suspend fun searchBasicInformation(planet: String) : String {
    return """
      The planet Jupiter is the fifth planet from the Sun and the largest in the Solar System. 
      It is a gas giant with a mass one-thousandth that of the Sun, but two-and-a-half times that 
      of all the other planets in the Solar System combined.
    """.trimIndent()
  }
}

/**
 * Interface for the Teacher agent.
 */
@Serializable
@System(
  instructions = [
    "You are a knowledgeable teacher who can explain complex concepts in simple terms.",
  ]
)
sealed interface Teacher {
  suspend fun explainConcept(
    @User(
      prompt = ["Explain the concept of gravitational pull in simple terms."]
    )
    concept: String
  ): String
}

/**
 * Interface for the Librarian agent.
 */
@Serializable
@System(
  instructions = [
    "You are a well-read librarian who can find information on various topics.",
  ],
  tools = [
    AstronomyTools::class
  ]
)
sealed interface Librarian {
  suspend fun findResources(
    @User(
      prompt = ["Find resources about {{topic}}"],
      tools = [AstronomyTools::class],
    )
    topic: String,
  ): List<String>
}

/**
 * Interface for the Student agent.
 */
@Serializable
@System(
  instructions = [
    "You are a curious student asking questions to learn more.",
  ]
)
sealed interface Student {
  suspend fun askQuestion(
    @User(
      prompt = ["I have a question about {{topic}}"]
    )
    topic: String
  ): String
}

suspend fun main() {
  val conversation = Conversation()
  val astronomer = Agents.agent<Astronomer>(conversation = conversation)
  val teacher = Agents.agent<Teacher>(conversation = conversation)
  val librarian = Agents.agent<Librarian>(conversation = conversation)
  val student = Agents.agent<Student>(conversation = conversation)

  val question = student.askQuestion("gravitational pull")
  println("Student: $question")

  val explanation = teacher.explainConcept("gravitational pull")
  println("Teacher: $explanation")

  val planetInfo = astronomer.getPlanetInfo("Jupiter")
  println("Astronomer: $planetInfo")

  val resources = librarian.findResources("gravitational pull")
  println("Librarian: Here are some resources you can check out:")
  resources.forEach { println(it) }
}
