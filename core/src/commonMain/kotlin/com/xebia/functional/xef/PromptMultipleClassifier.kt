package com.xebia.functional.xef

import com.xebia.functional.xef.conversation.Description
import kotlin.reflect.KType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.serializer

interface PromptMultipleClassifier {
  fun getItems(): List<Classification>

  fun template(input: String): String {
    val items = getItems()

    return """
       |Based on the <input>, identify whether the user is asking about one or more of the following items
       |
       |${
      items.joinToString("\n") { item -> "<${item.name}>${item.description}</${item.name}>" }
    }
       | 
       |<items>
       |${
      items.mapIndexed { index, item -> "\t<item index=\"$index\">${item.name}</item>" }
        .joinToString("\n")
    }
       |</items>
       |<input>
       |$input
       |</input>
    """
  }

  @OptIn(ExperimentalSerializationApi::class)
  fun KType.enumValuesName(serializer: KSerializer<Any?> = serializer(this)): List<Classification> {
    return if (serializer.descriptor.kind != SerialKind.ENUM) {
      emptyList()
    } else {
      (0 until serializer.descriptor.elementsCount).map { index ->
        val name =
          serializer.descriptor.getElementName(index).removePrefix(serializer.descriptor.serialName)
        val description =
          (serializer.descriptor.getElementAnnotations(index).first { it is Description }
              as Description)
            .value
        Classification(name, description)
      }
    }
  }
}
