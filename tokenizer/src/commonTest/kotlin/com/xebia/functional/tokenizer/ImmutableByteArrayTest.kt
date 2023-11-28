package com.xebia.functional.tokenizer

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test

class ImmutableByteArrayTest {
  @Test
  fun canBeUsedAsKeyInMap() {
    val key1 = ImmutableByteArray.from("1, 2, 3")
    val key2 = ImmutableByteArray.from("1, 2, 3")

    key1 shouldBe key2
    key1.hashCode() shouldBe key2.hashCode()
  }


  @Test
  fun cannotBeMutatedWhenUsingByteArrayConstructor() {
    val bytes = byteArrayOf(1, 2, 3)
    val byteArray = ImmutableByteArray.from(bytes)

    bytes[0] = 4

    byteArray shouldNotBe ImmutableByteArray.from(bytes)
    byteArray shouldBe ImmutableByteArray.from(byteArrayOf(1, 2, 3))
  }

  @Test
  fun cannotBeMutatedWhenUsingRawArray() {
    val byteArray = ImmutableByteArray.from("1, 2, 3")
    val bytes = byteArray.rawArray

    bytes[0] = 4

    byteArray shouldNotBe ImmutableByteArray.from(bytes)
    byteArray shouldBe ImmutableByteArray.from("1, 2, 3")
  }

  @Test
  fun getLengthIsCorrect() {
    val byteArray = ImmutableByteArray.from("1, 2, 3")

    byteArray.size shouldBe 7
  }

  @Test
  fun getBytesBetweenReturnsCorrectSliceOfArray() {
    val byteArray = ImmutableByteArray.from(byteArrayOf(1, 2, 3, 4, 5, 6))

    byteArray.getBytesBetween(3, 6) shouldBe ImmutableByteArray.from(byteArrayOf(4, 5, 6))
  }

  @Test
  fun getBytesBetweenThrowsWhenInclusiveStartIndexIsOutOfBounds() {
    val byteArray = ImmutableByteArray.from(byteArrayOf(1, 2, 3, 4, 5, 6))

    shouldThrow<IndexOutOfBoundsException> { byteArray.getBytesBetween(-1, 6) }
    shouldThrow<IndexOutOfBoundsException> { byteArray.getBytesBetween(9, 10) }
  }

  @Test
  fun getBytesBetweenThrowsWhenExclusiveEndIndexIsOutOfBounds() {
    val byteArray = ImmutableByteArray.from(byteArrayOf(1, 2, 3, 4, 5, 6))

    shouldThrow<IndexOutOfBoundsException> { byteArray.getBytesBetween(0, 7) }
    shouldThrow<IndexOutOfBoundsException> { byteArray.getBytesBetween(0, -1) }
  }

  @Test
  fun getBytesBetweenThrowsWhenStartIndexIsGreaterThanEndIndex() {
    val byteArray = ImmutableByteArray.from(byteArrayOf(1, 2, 3, 4, 5, 6))

    shouldThrow<IllegalArgumentException> { byteArray.getBytesBetween(3, 2) }
  }
}
