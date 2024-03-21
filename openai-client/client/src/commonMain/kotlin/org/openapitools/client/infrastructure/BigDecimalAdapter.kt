package org.openapitools.client.infrastructure

import java.math.BigDecimal
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = BigDecimal::class)
object BigDecimalAdapter : KSerializer<BigDecimal> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): BigDecimal = BigDecimal(decoder.decodeString())

  override fun serialize(encoder: Encoder, value: BigDecimal) =
    encoder.encodeString(value.toPlainString())
}
