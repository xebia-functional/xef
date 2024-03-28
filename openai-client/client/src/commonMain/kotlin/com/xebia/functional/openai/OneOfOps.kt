package com.xebia.functional.openai

import kotlin.reflect.KClass
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement

class OneOfSerializationException(
  val payload: JsonElement,
  val errors: Map<KClass<*>, SerializationException>,
  override val message: String =
    """
    Failed to deserialize Json: $payload.
    Errors: ${
    errors.entries.joinToString(separator = "\n") { (type, error) ->
      "$type - failed to deserialize: ${error.stackTraceToString()}"
    }
  }
    """
      .trimIndent()
) : SerializationException(message)

/**
 * OpenAI makes a lot of use of oneOf types (sum types, or unions types), but it **never** relies on
 * a discriminator field to differentiate between the types.
 *
 * Typically, what OpenAI does is attach a common field like `type` (a single value enum). I.e.
 * `MessageObjectContentInner` has a type field with `image` or `text`. Depending on the `type`
 * property, the other properties will be different.
 *
 * Due to the use of these fields, it **seems** there are no overlapping objects in the schema. So
 * to deserialize these types, we can try to deserialize each type and return the first one that
 * succeeds. In the case **all** fail, we throw [OneOfSerializationException] which includes all the
 * attempted types with their errors.
 *
 * This method relies on 'peeking', which is not possible in KotlinX Serialization. So to achieve
 * peeking, we first deserialize the raw Json to JsonElement, which safely consumes the buffer. And
 * then we can attempt to deserialize the JsonElement to the desired type, without breaking the
 * internal parser buffer.
 */
internal fun <A> attemptDeserialize(
  json: JsonElement,
  vararg block: Pair<KClass<*>, (json: JsonElement) -> A>
): A {
  val errors = linkedMapOf<KClass<*>, SerializationException>()
  block.forEach { (kclass, f) ->
    try {
      return f(json)
    } catch (e: SerializationException) {
      errors[kclass] = e
    }
  }
  throw OneOfSerializationException(json, errors)
}
