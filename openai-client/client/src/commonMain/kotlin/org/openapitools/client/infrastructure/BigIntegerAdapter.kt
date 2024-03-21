package org.openapitools.client.infrastructure

import java.math.BigInteger
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = BigInteger::class)
object BigIntegerAdapter : KSerializer<BigInteger> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("BigInteger", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): BigInteger {
    return BigInteger(decoder.decodeString())
  }

  override fun serialize(encoder: Encoder, value: BigInteger) {
    encoder.encodeString(value.toString())
  }
}
