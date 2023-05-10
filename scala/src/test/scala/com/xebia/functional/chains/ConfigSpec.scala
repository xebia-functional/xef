package com.xebia.functional.scala.chains

import cats.effect.IO

import com.xebia.functional.scala.chains.models.Config
import com.xebia.functional.scala.chains.models.InvalidChainInputError
import com.xebia.functional.scala.chains.models.InvalidChainInputsError
import munit.CatsEffectSuite

class ConfigSpec extends CatsEffectSuite:

  test("Chain Config should return the inputs properly") {
    val config = Config(Set("name", "age"), Set("text"), false)
    val result = config.genInputs[IO](Map("name" -> "foo", "age" -> "bar"))
    assertIO(result, Map("name" -> "foo", "age" -> "bar"))
  }

  test("Chain Config should return the input as a Map") {
    val config = Config(Set("input"), Set("text"), false)
    val result = config.genInputsFromString[IO]("foo")
    assertIO(result, Map("input" -> "foo"))
  }

  test("Chain Config should fail when inputs set doesn't contain all inputKeys") {
    val config = Config(Set("name", "age"), Set("text"), false)
    val result = config.genInputs[IO](Map("name" -> "foo"))
    interceptMessageIO[InvalidChainInputsError]("The provided inputs (name) do not match with chain's inputs (name, age)")(result)
  }

  test("Chain Config should fail when inputs set has different inputKeys") {
    val config = Config(Set("name", "age"), Set("text"), false)
    val result = config.genInputs[IO](Map("name" -> "foo", "city" -> "NY"))
    interceptMessageIO[InvalidChainInputsError](
      "The provided inputs (name, city) do not match with chain's inputs (name, age)"
    )(result)
  }

  test("Chain Config should fail when input is just one and expects more") {
    val config = Config(Set("name", "age"), Set("text"), false)
    val result = config.genInputsFromString[IO]("foo")
    interceptMessageIO[InvalidChainInputError]("The expected inputs are more than one: name, age")(result)
  }
