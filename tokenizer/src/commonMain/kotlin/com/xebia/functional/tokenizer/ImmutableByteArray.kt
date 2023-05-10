package com.xebia.functional.tokenizer

import kotlin.jvm.JvmInline

@JvmInline
internal value class ImmutableByteArray private constructor(private val array: ByteArray) {

  val size: Int
    inline get() = array.size

  /**
   * Returns the bytes of this array from startIndex (inclusive) to endIndex (exclusive). The returned array is a copy
   * of the original array.
   *
   * @param startIndex the index from which to start copying (inclusive)
   * @param endIndex   the index at which to stop copying (exclusive)
   * @return a new [ImmutableByteArray] containing the bytes from startIndex (inclusive) to endIndex (exclusive)
   * @throws IllegalArgumentException if startIndex is out of bounds, endIndex is out of bounds or endIndex is less than
   * startIndex
   */
  fun getBytesBetween(startIndex: Int, endIndex: Int): ImmutableByteArray {
    if (startIndex < 0 || startIndex >= array.size) throw IndexOutOfBoundsException("startIndex out of bounds: $startIndex ($this)")
    if (endIndex < 0 || endIndex > array.size) throw IndexOutOfBoundsException("endIndex out of bounds: $endIndex ($this)")
    require(startIndex < endIndex) { "startIndex must be less than endIndex: $startIndex >= $endIndex" }

    val length = endIndex - startIndex
    val result = ByteArray(length)
    array.copyInto(result, startIndex = startIndex, endIndex = endIndex)
    return ImmutableByteArray(result)
  }

  val rawArray: ByteArray
    inline get() = array.copyOf()

  @Suppress("RESERVED_MEMBER_INSIDE_VALUE_CLASS")
  override fun equals(other: Any?): Boolean =
    when {
      other !is ImmutableByteArray -> false
      array === other.array -> true
      else -> array.contentEquals(other.array)
    }

  @Suppress("RESERVED_MEMBER_INSIDE_VALUE_CLASS")
  override fun hashCode(): Int = array.contentHashCode()

  override fun toString(): String = buildString {
    append("[")
    var count = 0
    for (i in array) {
      if (++count > 1) append(", ")
      append(i)
    }
    append("]")
  }

  companion object {
    fun from(array: ByteArray): ImmutableByteArray =
      ImmutableByteArray(array.copyOf())

    internal fun from(string: String): ImmutableByteArray =
      ImmutableByteArray(string.encodeToByteArray())
  }
}
