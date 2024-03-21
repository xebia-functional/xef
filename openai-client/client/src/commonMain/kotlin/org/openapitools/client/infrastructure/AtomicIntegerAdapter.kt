package org.openapitools.client.infrastructure

import java.util.concurrent.atomic.AtomicInteger
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = AtomicInteger::class)
object AtomicIntegerAdapter : KSerializer<AtomicInteger> {
  override fun serialize(encoder: Encoder, value: AtomicInteger) {
    encoder.encodeInt(value.get())
  }

  override fun deserialize(decoder: Decoder): AtomicInteger = AtomicInteger(decoder.decodeInt())

  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("AtomicInteger", PrimitiveKind.INT)
}
