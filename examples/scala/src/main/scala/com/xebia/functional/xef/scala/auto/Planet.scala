package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder
import io.circe.parser.decode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.BuiltinSerializersKt
import kotlinx.serialization.encoding.{Decoder as DecoderX, Encoder as EncoderX}

import scala.compiletime.summonInline

private final class MoonSerializer extends KSerializer[Moon] {
  override def getDescriptor = summonInline[ScalaSerialDescriptor[Moon]].serialDescriptor
  override def serialize(encoder: EncoderX, m: Moon): Unit = ???
  override def deserialize(decoder: DecoderX): Moon = ???
}

given ScalaSerialDescriptor[List[Moon]] = new ScalaSerialDescriptor[List[Moon]]:
  def serialDescriptor = BuiltinSerializersKt.ListSerializer(new MoonSerializer).getDescriptor

private final case class Moon(name: String, distanceFromPlanet: Double) derives ScalaSerialDescriptor, Decoder

private final case class Planet(name: String, distanceFromSun: Double, moons: List[Moon]) derives ScalaSerialDescriptor, Decoder

@main def runPlanet: Unit =

  def planetInfo(planet: Planet): String = {
    s"""${planet.name} is ${planet.distanceFromSun} million km away from the Sun.
       |It has the following moons:
       |${planet.moons.map(it => s"${it.name}: ${it.distanceFromPlanet} km away from ${planet.name}").mkString("\n")}
    """.stripMargin
  }
  ai {
    val earth: Planet = prompt("Information about Earth and its moon.")
    val mars: Planet = prompt("Information about Mars and its moons.")

    println(s"Celestial bodies information:\n\n${planetInfo(earth)}\n\n${planetInfo(mars)}")
  }
