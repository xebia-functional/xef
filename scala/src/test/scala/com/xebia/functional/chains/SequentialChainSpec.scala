package com.xebia.functional.chains

import cats.data.NonEmptySeq
import cats.data.NonEmptySet
import cats.effect.IO
import cats.implicits.*
import cats.kernel.Order

import com.xebia.functional.chains.mock.FakeChain
import com.xebia.functional.chains.models.*
import eu.timepit.refined.types.string.NonEmptyString
import munit.CatsEffectSuite

class SequentialChainSpec extends CatsEffectSuite:

  given Order[NonEmptyString] = Order.fromComparable[String].contramap(_.value)

  test("Test SequentialChain on single input chain") {
    val chain1 = FakeChain(inputVariables = Set("foo"), outputVariables = Set("bar"))
    val chain2 = FakeChain(inputVariables = Set("bar"), outputVariables = Set("baz"))
    val chains = NonEmptySeq(chain1, Seq(chain2))

    val inputKey = NonEmptyString.unsafeFrom("foo")
    val outputKey = NonEmptyString.unsafeFrom("baz")

    val sc = SequentialChain.resource[IO](chains, NonEmptySet.of(inputKey), NonEmptySet.of(outputKey))

    val output = sc.use(_.run(Map("foo" -> "123")))
    val expectedOutput = Map("baz" -> "123foofoo", "foo" -> "123")
    assertIO(output, expectedOutput)
  }

  test("Test SequentialChain on multiple input chains") {
    val chain1 = FakeChain(inputVariables = Set("foo", "test"), outputVariables = Set("bar"))
    val chain2 = FakeChain(inputVariables = Set("bar", "foo"), outputVariables = Set("baz"))
    val chains = NonEmptySeq(chain1, Seq(chain2))

    val inputKey0 = NonEmptyString.unsafeFrom("foo")
    val inputKey1 = NonEmptyString.unsafeFrom("test")
    val outputKey = NonEmptyString.unsafeFrom("baz")

    val sc = SequentialChain.resource[IO](chains, NonEmptySet.of(inputKey0, inputKey1), NonEmptySet.of(outputKey))

    val output = sc.use(_.run(Map("foo" -> "123", "test" -> "456")))
    val expectedOutput = Map("baz" -> "123456foo123foo", "foo" -> "123", "test" -> "456")
    assertIO(output, expectedOutput)
  }

  test("Test SequentialChain on multiple output chains") {
    val chain1 = FakeChain(inputVariables = Set("foo"), outputVariables = Set("bar", "test"))
    val chain2 = FakeChain(inputVariables = Set("bar", "foo"), outputVariables = Set("baz"))
    val chains = NonEmptySeq(chain1, Seq(chain2))

    val inputKey0 = NonEmptyString.unsafeFrom("foo")
    val outputKey = NonEmptyString.unsafeFrom("baz")

    val sc = SequentialChain.resource[IO](chains, NonEmptySet.of(inputKey0), NonEmptySet.of(outputKey))

    val output = sc.use(_.run(Map("foo" -> "123")))
    val expectedOutput = Map("baz" -> "123foo123foo", "foo" -> "123")
    assertIO(output, expectedOutput)
  }

  test("Test SequentialChain error is raised when input variables are missing.") {
    val chain1 = FakeChain(inputVariables = Set("foo"), outputVariables = Set("bar"))
    val chain2 = FakeChain(inputVariables = Set("bar", "test"), outputVariables = Set("baz"))
    val chains = NonEmptySeq(chain1, Seq(chain2))

    val inputKey0 = NonEmptyString.unsafeFrom("foo")
    val outputKey = NonEmptyString.unsafeFrom("baz")

    val sc = SequentialChain.resource[IO](chains, NonEmptySet.of(inputKey0), NonEmptySet.of(outputKey), true)

    val output = sc.use(_.run(Map("foo" -> "123")))
    interceptIO[MissingInputVariablesError](output)
  }

  test("Test SequentialChain error is raised when bad outputs are specified.") {
    val chain1 = FakeChain(inputVariables = Set("foo"), outputVariables = Set("bar"))
    val chain2 = FakeChain(inputVariables = Set("bar"), outputVariables = Set("baz"))
    val chains = NonEmptySeq(chain1, Seq(chain2))

    val inputKey0 = NonEmptyString.unsafeFrom("foo")
    val outputKey = NonEmptyString.unsafeFrom("test")

    val sc = SequentialChain.resource[IO](chains, NonEmptySet.of(inputKey0), NonEmptySet.of(outputKey), true)

    val output = sc.use(_.run(Map("foo" -> "123")))
    interceptIO[MissingOutputVariablesError](output)
  }

  test("Test SequentialChainruns when valid outputs are specified.") {
    val chain1 = FakeChain(inputVariables = Set("foo"), outputVariables = Set("bar"))
    val chain2 = FakeChain(inputVariables = Set("bar"), outputVariables = Set("baz"))
    val chains = NonEmptySeq(chain1, Seq(chain2))

    val inputKey = NonEmptyString.unsafeFrom("foo")
    val outputKey0 = NonEmptyString.unsafeFrom("bar")
    val outputKey1 = NonEmptyString.unsafeFrom("baz")

    val sc = SequentialChain.resource[IO](chains, NonEmptySet.of(inputKey), NonEmptySet.of(outputKey0, outputKey1), true)

    val output = sc.use(_.run(Map("foo" -> "123")))
    val expectedOutput = Map("baz" -> "123foofoo", "bar" -> "123foo")
    assertIO(output, expectedOutput)
  }

  test("Test SequentialChainruns error is raised when input variables are overlapping.") {
    val chain1 = FakeChain(inputVariables = Set("foo"), outputVariables = Set("bar", "test"))
    val chain2 = FakeChain(inputVariables = Set("bar"), outputVariables = Set("baz"))
    val chains = NonEmptySeq(chain1, Seq(chain2))

    val inputKey0 = NonEmptyString.unsafeFrom("foo")
    val inputKey1 = NonEmptyString.unsafeFrom("test")
    val outputKey = NonEmptyString.unsafeFrom("output")

    val sc = SequentialChain.resource[IO](chains, NonEmptySet.of(inputKey0, inputKey1), NonEmptySet.of(outputKey), true)

    val output = sc.use(_.run(Map("foo" -> "123")))
    interceptIO[OverlappingInputError](output)
  }
