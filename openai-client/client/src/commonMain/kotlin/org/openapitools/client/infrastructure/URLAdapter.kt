package org.openapitools.client.infrastructure

import java.net.URL
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = URL::class)
object URLAdapter : KSerializer<URL> {
  override fun serialize(encoder: Encoder, value: URL) {
    encoder.encodeString(value.toExternalForm())
  }

  override fun deserialize(decoder: Decoder): URL = URL(decoder.decodeString())

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("URL", PrimitiveKind.STRING)
}
