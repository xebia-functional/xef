package com.xebia.functional.tokenizer

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ImmutableByteArrayTest : StringSpec({
  "can be used as key in map" {
    val key1 = ImmutableByteArray.from("1, 2, 3")
    val key2 = ImmutableByteArray.from("1, 2, 3")

    key1 shouldBe key2
    key1.hashCode() shouldBe key2.hashCode()
  }

  "cannot be mutated when using ByteArray constructor" {
    val bytes = byteArrayOf(1, 2, 3)
    val byteArray = ImmutableByteArray.from(bytes)

    bytes[0] = 4

    byteArray shouldNotBe ImmutableByteArray.from(bytes)
    byteArray shouldBe ImmutableByteArray.from(byteArrayOf(1, 2, 3))
  }

  "cannot be mutated when using rawArray" {
    val byteArray = ImmutableByteArray.from("1, 2, 3")
    val bytes = byteArray.rawArray

    bytes[0] = 4

    byteArray shouldNotBe ImmutableByteArray.from(bytes)
    byteArray shouldBe ImmutableByteArray.from("1, 2, 3")
  }

  "getLength is correct" {
    val byteArray = ImmutableByteArray.from("1, 2, 3")

    byteArray.size shouldBe 7
  }

  "getBytesBetween returns correct slice of array" {
    val byteArray = ImmutableByteArray.from(byteArrayOf(1, 2, 3, 4, 5, 6))

    byteArray.getBytesBetween(3, 6) shouldBe ImmutableByteArray.from(byteArrayOf(4, 5, 6))
  }

  "getBytesBetween throws when inclusive startIndex is out of bounds" {
    val byteArray = ImmutableByteArray.from(byteArrayOf(1, 2, 3, 4, 5, 6))

    shouldThrow<IndexOutOfBoundsException> { byteArray.getBytesBetween(-1, 6) }
    shouldThrow<IndexOutOfBoundsException> { byteArray.getBytesBetween(9, 10) }
  }

  "getBytesBetween throws when exclusive endIndex is out of bounds" {
    val byteArray = ImmutableByteArray.from(byteArrayOf(1, 2, 3, 4, 5, 6))

    shouldThrow<IndexOutOfBoundsException> { byteArray.getBytesBetween(0, 7) }
    shouldThrow<IndexOutOfBoundsException> { byteArray.getBytesBetween(0, -1) }
  }

  "getBytesBetween throws when startIndex is greater than endIndex" {
    val byteArray = ImmutableByteArray.from(byteArrayOf(1, 2, 3, 4, 5, 6))

    shouldThrow<IllegalArgumentException> { byteArray.getBytesBetween(3, 2) }
  }
})
