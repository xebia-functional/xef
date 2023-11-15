package ai.xef.openai

import com.xebia.functional.tokenizer.ModelType
import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = OpenAIModelSerializer::class)
sealed interface OpenAIModel<T> {

  fun value(): String =
    when (this) {
      is CustomModel -> model
      is StandardModel -> model.toString()
    }

  fun modelType(): ModelType {
    val stringValue = value()
    return ModelType.all.find { it.name == stringValue } ?: ModelType.TODO(stringValue)
  }
}

@Serializable @JvmInline value class CustomModel<T>(val model: String) : OpenAIModel<T>

@Serializable @JvmInline value class StandardModel<T>(val model: T) : OpenAIModel<T>

class OpenAIModelSerializer<T>(private val dataSerializer: KSerializer<T>) :
  KSerializer<OpenAIModel<T>> {
  override val descriptor: SerialDescriptor = OpenAIModel.serializer(dataSerializer).descriptor

  override fun serialize(encoder: Encoder, value: OpenAIModel<T>) =
    when (value) {
      is CustomModel<T> -> String.serializer().serialize(encoder, value.model)
      is StandardModel<T> -> dataSerializer.serialize(encoder, value.model)
    }

  override fun deserialize(decoder: Decoder) =
    try {
      StandardModel(dataSerializer.deserialize(decoder))
    } catch (e: SerializationException) {
      CustomModel(String.serializer().deserialize(decoder))
    } catch (e: IllegalArgumentException) {
      CustomModel(String.serializer().deserialize(decoder))
    }
}
