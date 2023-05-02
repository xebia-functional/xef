package com.xebia.functional.chains

import arrow.core.Either
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ConfigSpec : StringSpec({

  "Chain Config should return the inputs properly" {
    val config = Chain.Config(setOf("name", "age"), setOf("text"))
    val result = config.createInputs(mapOf("name" to "foo", "age" to "bar"))
    result shouldBe Either.Right(mapOf("name" to "foo", "age" to "bar"))
  }

  "Chain Config should return the input as a Map" {
    val config = Chain.Config(setOf("input"), setOf("text"))
    val result = config.createInputs("foo")
    result shouldBe Either.Right(mapOf("input" to "foo"))
  }

  "Chain Config should fail when inputs set doesn't contain all inputKeys" {
    val config = Chain.Config(setOf("name", "age"), setOf("text"))
    val result = config.createInputs(mapOf("name" to "foo"))
    result shouldBe Either.Left(
      Chain.InvalidInputs("The provided inputs: {name} do not match with chain's inputs: {name}, {age}")
    )
  }

  "Chain Config should fail when inputs set has different inputKeys" {
    val config = Chain.Config(setOf("name", "age"), setOf("text"))
    val result = config.createInputs(mapOf("name" to "foo", "city" to "NY"))
    result shouldBe Either.Left(
      Chain.InvalidInputs("The provided inputs: {name}, {city} do not match with chain's inputs: {name}, {age}")
    )
  }

  "Chain Config should fail when input is just one and expects more" {
    val config = Chain.Config(setOf("name", "age"), setOf("text"))
    val result = config.createInputs("foo")
    result shouldBe Either.Left(
      Chain.InvalidInputs("The expected inputs are more than one: {name}, {age}")
    )
  }
})
