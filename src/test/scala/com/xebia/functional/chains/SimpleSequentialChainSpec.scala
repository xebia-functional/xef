package com.xebia.functional.chains

import cats.effect.IO
import cats.implicits.*
import munit.CatsEffectSuite
import com.xebia.functional.chains.mock.FakeChain
import cats.data.NonEmptySeq
import eu.timepit.refined.types.string.NonEmptyString
import com.xebia.functional.chains.models.*

class SimpleSequentialChainSpec extends CatsEffectSuite:
  test("SimpleSequentialChain functionality") {
    val chain1 = FakeChain(inputVariables = Set("foo"), outputVariables = Set("bar"))
    val chain2 = FakeChain(inputVariables = Set("bar"), outputVariables = Set("baz"))
    val chains = NonEmptySeq(chain1, Seq(chain2))

    val inputKey = NonEmptyString.from("input").toOption.get
    val outputKey = NonEmptyString.from("output").toOption.get

    val ssc = SimpleSequentialChain.resource[IO](chains, inputKey, outputKey)

    val output = ssc.use(_.run(Map("input" -> "123")))
    val expectedOutput = Map("output" -> "123foofoo", "input" -> "123")
    assertIO(output, expectedOutput)
  }

  test("SimpleSequentialChain should fail if multiple input variables are expected") {
    val chain1 = FakeChain(inputVariables = Set("foo"), outputVariables = Set("bar"))
    val chain2 = FakeChain(inputVariables = Set("bar", "foo"), outputVariables = Set("baz"))
    val chains = NonEmptySeq(chain1, Seq(chain2))

    val inputKey = NonEmptyString.from("input").toOption.get
    val outputKey = NonEmptyString.from("output").toOption.get

    val ssc = SimpleSequentialChain.resource[IO](chains, inputKey, outputKey)

    val output = ssc.use(_.run(Map("input" -> "123")))

    interceptIO[InvalidChainInputError](output)
  }

  test("SimpleSequentialChain should fail if multiple outputs variables are expected") {
    val chain1 = FakeChain(inputVariables = Set("foo"), outputVariables = Set("bar", "becue"))
    val chain2 = FakeChain(inputVariables = Set("bar"), outputVariables = Set("baz"))
    val chains = NonEmptySeq(chain1, Seq(chain2))

    val inputKey = NonEmptyString.from("input").toOption.get
    val outputKey = NonEmptyString.from("output").toOption.get

    val ssc = SimpleSequentialChain.resource[IO](chains, inputKey, outputKey)

    val output = ssc.use(_.run(Map("input" -> "123")))

    interceptIO[InvalidChainOutputError](output)
  }
