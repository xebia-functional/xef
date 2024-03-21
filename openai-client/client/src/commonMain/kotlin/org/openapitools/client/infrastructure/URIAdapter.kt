package org.openapitools.client.infrastructure

import java.net.URI
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(forClass = URI::class)
object URIAdapter : KSerializer<URI> {
  override fun serialize(encoder: Encoder, value: URI) {
    encoder.encodeString(value.toASCIIString())
  }

  override fun deserialize(decoder: Decoder): URI = URI(decoder.decodeString())

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("URI", PrimitiveKind.STRING)
}
