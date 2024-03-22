package com.xebia.functional.tokenizer

/**
 * Creates a new TokenEncoder with the given input map. The keys of the map are
 * the decoded tokens and the values are the encoded tokens. The keyMapper is
 * applied to the keys of the input map before they are added to the internal
 * maps.
 *
 * @param input     the input map
 * @param keyMapper the key mapper
 */
internal fun <T, K, V> TokenEncoder(
  input: Map<T, V>,
  keyMapper: (T) -> K
): TokenEncoder<K, V> {
  val decodedToEncoded = mutableMapOf<K, V>()
  val encodedToDecoded = mutableMapOf<V, K>()
  for ((key1, value) in input) {
    val key: K = keyMapper(key1)
    decodedToEncoded[key] = value
    encodedToDecoded[value] = key
  }
  return TokenEncoder(decodedToEncoded, encodedToDecoded)
}

/**
 * Creates a new TokenEncoder with the given input map. The keys of the map are
 * the decoded tokens and the values are the encoded tokens.
 *
 * @param input the input map
 */
internal fun <K, V> TokenEncoder (
  input: Map<K, V>,
): TokenEncoder<K, V> =
  TokenEncoder(input) { it }

/**
 * A TokenEncoder is used to encode and decode tokens. It is initialized with a map
 * that contains the decoded tokens as keys and the encoded tokens as values. The
 * TokenEncoder can then be used to encode and decode tokens.
 *
 * @param <K> the type of the decoded tokens
 * @param <V> the type of the encoded tokens
</V></K> */
internal class TokenEncoder<K, V>(
  private val decodedToEncoded: MutableMap<K, V>,
  private val encodedToDecoded: MutableMap<V, K>
) {
  /**
   * Checks if the given decoded token is contained in this encoder.
   *
   * @param decodedToken the decoded token
   * @return true if the decoded token is contained in this encoder, false otherwise
   */
  fun containsDecodedToken(decodedToken: K): Boolean =
    decodedToEncoded.containsKey(decodedToken)

  /**
   * Encodes the given decoded token.
   *
   * @param decodedToken the decoded token
   * @return the encoded token
   * @throws IllegalArgumentException if the decoded token is not contained in this encoder
   */
  fun encode(decodedToken: K): V =
    requireNotNull(decodedToEncoded[decodedToken]) { "Unknown token for encoding: $decodedToken" }

  /**
   * Encodes the given decoded token if it is contained in this encoder. Otherwise,
   * an empty optional is returned.
   *
   * @param decodedToken the decoded token
   * @return the encoded token or an empty optional
   */
  fun encodeIfPresent(decodedToken: K): V? =
    decodedToEncoded[decodedToken]

  /**
   * Decodes the given encoded token if it is contained in this encoder. Otherwise,
   * an empty optional is returned.
   *
   * @param encodedToken the encoded token
   * @return the decoded token or an empty optional
   */
  fun decodeIfPresent(encodedToken: V): K? =
    encodedToDecoded[encodedToken]

  val decodedTokens: Set<K> = decodedToEncoded.keys
}
