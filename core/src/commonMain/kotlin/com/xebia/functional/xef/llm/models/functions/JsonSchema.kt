@file:JvmName("Json")
@file:JvmMultifileClass
@file:OptIn(ExperimentalSerializationApi::class)

package com.xebia.functional.xef.llm.models.functions

/*
Ported over from https://github.com/Ricky12Awesome/json-schema-serialization
which states the following:

> I lost interest in this project, that why there hasn't been any changes for
> over 2 years if anyone wants to maintain this project or a fork of this project, then I will update this readme

// TODO: We should consider a fork and maintain it ourselves.
 */
import com.xebia.functional.xef.conversation.Description
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.json.*

/** Represents the type of json type */
enum class JsonType(jsonType: String) {
  /** Represents the json array type */
  ARRAY("array"),

  /** Represents the json number type */
  NUMBER("number"),

  /** Represents the string type */
  STRING("string"),

  /** Represents the boolean type */
  BOOLEAN("boolean"),

  /** Represents the object type, this is used for serializing normal classes */
  OBJECT("object"),

  /** Represents the object type, this is used for serializing sealed classes */
  OBJECT_SEALED("object"),

  /** Represents the object type, this is used for serializing maps */
  OBJECT_MAP("object");

  val json = JsonPrimitive(jsonType)

  override fun toString(): String = json.content
}

@Target()
annotation class JsonSchema {

  /** Enum-like values for non-enum string */
  @SerialInfo
  @Retention(AnnotationRetention.BINARY)
  @Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
  annotation class StringEnum(val values: Array<out String>)

  /**
   * Minimum and Maximum values using whole numbers
   *
   * Only works when [SerialKind] is any of [PrimitiveKind.BYTE], [PrimitiveKind.SHORT],
   * [PrimitiveKind.INT], [PrimitiveKind.LONG]
   */
  @SerialInfo
  @Retention(AnnotationRetention.BINARY)
  @Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
  annotation class IntRange(val min: Long, val max: Long)

  /**
   * Minimum and Maximum values using floating point numbers
   *
   * Only works when [SerialKind] is [PrimitiveKind.FLOAT] or [PrimitiveKind.DOUBLE]
   */
  @SerialInfo
  @Retention(AnnotationRetention.BINARY)
  @Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
  annotation class FloatRange(val min: Double, val max: Double)

  /**
   * [pattern] to use on this property
   *
   * Only works when [SerialKind] is [PrimitiveKind.STRING]
   */
  @SerialInfo
  @Retention(AnnotationRetention.BINARY)
  @Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
  annotation class Pattern(val pattern: String)

  /**
   * Should this property be a definition and be referenced using [id]?
   *
   * @param id The id for this definition, this will be referenced by '#/definitions/$[id]'
   */
  @SerialInfo
  @Retention(AnnotationRetention.BINARY)
  @Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
  annotation class Definition(val id: String)

  /** This property will not create definitions */
  @SerialInfo
  @Retention(AnnotationRetention.BINARY)
  @Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
  annotation class NoDefinition
}

/** Creates a Json Schema using the provided [descriptor] */
fun buildJsonSchema(descriptor: SerialDescriptor): JsonObject {
  val autoDefinitions = false
  val prepend = mapOf("\$schema" to JsonPrimitive("http://json-schema.org/draft-07/schema"))
  val definitions = JsonSchemaDefinitions(autoDefinitions)
  val root = descriptor.createJsonSchema(descriptor.annotations, definitions)
  val append = mapOf("definitions" to definitions.getDefinitionsAsJsonObject())

  return JsonObject(prepend + root + append)
}

private inline val SerialDescriptor.jsonLiteral
  inline get() = kind.jsonType.json

private inline val SerialKind.jsonType: JsonType
  inline get() =
    when (this) {
      StructureKind.LIST -> JsonType.ARRAY
      StructureKind.MAP -> JsonType.OBJECT_MAP
      PolymorphicKind.SEALED -> JsonType.OBJECT_SEALED
      PrimitiveKind.BYTE,
      PrimitiveKind.SHORT,
      PrimitiveKind.INT,
      PrimitiveKind.LONG,
      PrimitiveKind.FLOAT,
      PrimitiveKind.DOUBLE -> JsonType.NUMBER
      PrimitiveKind.STRING,
      PrimitiveKind.CHAR,
      SerialKind.ENUM -> JsonType.STRING
      PrimitiveKind.BOOLEAN -> JsonType.BOOLEAN
      else -> JsonType.OBJECT
    }

private inline fun <reified T> List<Annotation>.lastOfInstance(): T? =
  filterIsInstance<T>().lastOrNull()

private fun SerialDescriptor.jsonSchemaObject(definitions: JsonSchemaDefinitions): JsonObject {
  val properties = mutableMapOf<String, JsonElement>()
  val required = mutableListOf<JsonPrimitive>()

  elementDescriptors.forEachIndexed { index, child ->
    val name = getElementName(index)
    val annotations = getElementAnnotations(index)

    properties[name] = child.createJsonSchema(annotations, definitions)

    if (!isElementOptional(index)) {
      required += JsonPrimitive(name)
    }
  }

  return jsonSchemaElement(annotations) {
    if (properties.isNotEmpty()) {
      it["properties"] = JsonObject(properties)
    }

    if (required.isNotEmpty()) {
      it["required"] = JsonArray(required)
    }
  }
}

private fun SerialDescriptor.jsonSchemaObjectMap(definitions: JsonSchemaDefinitions): JsonObject {
  return jsonSchemaElement(annotations, skipNullCheck = false) {
    val (key, value) = elementDescriptors.toList()

    require(key.kind == PrimitiveKind.STRING) { "cannot have non string keys in maps" }

    it["additionalProperties"] = value.createJsonSchema(getElementAnnotations(1), definitions)
  }
}

private fun SerialDescriptor.jsonSchemaObjectSealed(
  definitions: JsonSchemaDefinitions
): JsonObject {
  val properties = mutableMapOf<String, JsonElement>()
  val required = mutableListOf<JsonPrimitive>()
  val anyOf = mutableListOf<JsonElement>()

  val (_, value) = elementDescriptors.toList()

  properties["type"] = buildJson {
    it["type"] = JsonType.STRING.json
    it["enum"] = value.elementNames
  }

  required += JsonPrimitive("type")

  if (isNullable) {
    anyOf += buildJson { nullable -> nullable["type"] = "null" }
  }

  value.elementDescriptors.forEachIndexed { index, child ->
    val schema = child.createJsonSchema(value.getElementAnnotations(index), definitions)
    val newSchema =
      schema.mapValues { (name, element) ->
        if (element is JsonObject && name == "properties") {
          val prependProps = mutableMapOf<String, JsonElement>()

          prependProps["type"] = buildJson { it["const"] = child.serialName }

          JsonObject(prependProps + element)
        } else {
          element
        }
      }

    anyOf += JsonObject(newSchema)
  }

  return jsonSchemaElement(annotations, skipNullCheck = true, skipTypeCheck = true) {
    if (properties.isNotEmpty()) {
      it["properties"] = JsonObject(properties)
    }

    if (anyOf.isNotEmpty()) {
      it["anyOf"] = JsonArray(anyOf)
    }

    if (required.isNotEmpty()) {
      it["required"] = JsonArray(required)
    }
  }
}

private fun SerialDescriptor.jsonSchemaArray(
  annotations: List<Annotation> = listOf(),
  definitions: JsonSchemaDefinitions
): JsonObject =
  jsonSchemaElement(annotations) {
    val type = getElementDescriptor(0)

    it["items"] = type.createJsonSchema(getElementAnnotations(0), definitions)
  }

private fun SerialDescriptor.jsonSchemaString(
  annotations: List<Annotation> = listOf()
): JsonObject {
  return jsonSchemaElement(annotations) {
    val pattern = annotations.lastOfInstance<JsonSchema.Pattern>()?.pattern ?: ""
    val enum = annotations.lastOfInstance<JsonSchema.StringEnum>()?.values ?: arrayOf()

    if (pattern.isNotEmpty()) {
      it["pattern"] = pattern
    }

    if (enum.isNotEmpty()) {
      it["enum"] = enum.toList()
    }
  }
}

private fun SerialDescriptor.jsonSchemaNumber(
  annotations: List<Annotation> = listOf()
): JsonObject =
  jsonSchemaElement(annotations) {
    val value =
      when (kind) {
        PrimitiveKind.FLOAT,
        PrimitiveKind.DOUBLE ->
          annotations.lastOfInstance<JsonSchema.FloatRange>()?.let {
            it.min as Number to it.max as Number
          }
        PrimitiveKind.BYTE,
        PrimitiveKind.SHORT,
        PrimitiveKind.INT,
        PrimitiveKind.LONG ->
          annotations.lastOfInstance<JsonSchema.IntRange>()?.let {
            it.min as Number to it.max as Number
          }
        else -> error("$kind is not a Number")
      }

    value?.let { (min, max) ->
      it["minimum"] = min
      it["maximum"] = max
    }
  }

private fun SerialDescriptor.jsonSchemaBoolean(
  annotations: List<Annotation> = listOf()
): JsonObject = jsonSchemaElement(annotations)

private fun SerialDescriptor.createJsonSchema(
  annotations: List<Annotation>,
  definitions: JsonSchemaDefinitions
): JsonObject {
  val combinedAnnotations = annotations + this.annotations
  val key = JsonSchemaDefinitions.Key(this, combinedAnnotations)

  return when (kind.jsonType) {
    JsonType.NUMBER -> definitions.get(key) { jsonSchemaNumber(combinedAnnotations) }
    JsonType.STRING -> definitions.get(key) { jsonSchemaString(combinedAnnotations) }
    JsonType.BOOLEAN -> definitions.get(key) { jsonSchemaBoolean(combinedAnnotations) }
    JsonType.ARRAY -> definitions.get(key) { jsonSchemaArray(combinedAnnotations, definitions) }
    JsonType.OBJECT -> definitions.get(key) { jsonSchemaObject(definitions) }
    JsonType.OBJECT_MAP -> definitions.get(key) { jsonSchemaObjectMap(definitions) }
    JsonType.OBJECT_SEALED -> definitions.get(key) { jsonSchemaObjectSealed(definitions) }
  }
}

@OptIn(ExperimentalSerializationApi::class)
private fun JsonObjectBuilder.applyJsonSchemaDefaults(
  descriptor: SerialDescriptor,
  annotations: List<Annotation>,
  skipNullCheck: Boolean = false,
  skipTypeCheck: Boolean = false
) {
  if (descriptor.isNullable && !skipNullCheck) {
    this["if"] = buildJson { it["type"] = descriptor.jsonLiteral }
    this["else"] = buildJson { it["type"] = "null" }
  } else {
    if (!skipTypeCheck) {
      this["type"] = descriptor.jsonLiteral
    }
  }

  val additionalEnumDescription: String? =
    if (descriptor.kind == SerialKind.ENUM) {
      this["enum"] = descriptor.elementNames
      descriptor.elementNames
        .mapIndexedNotNull { index, name ->
          val enumDescription =
            descriptor.getElementAnnotations(index).lastOfInstance<Description>()?.value
          if (enumDescription != null) {
            "$name ($enumDescription)"
          } else {
            null
          }
        }
        .joinToString("\n - ")
    } else null

  if (annotations.isNotEmpty()) {
    val description = annotations.filterIsInstance<Description>().firstOrNull()?.value
    if (!additionalEnumDescription.isNullOrEmpty()) {
      this["description"] = "$description\n - $additionalEnumDescription"
    } else {
      this["description"] = description
    }
  } else if (additionalEnumDescription != null) {
    this["description"] = " - $additionalEnumDescription"
  }
}

private inline fun SerialDescriptor.jsonSchemaElement(
  annotations: List<Annotation>,
  skipNullCheck: Boolean = false,
  skipTypeCheck: Boolean = false,
  applyDefaults: Boolean = true,
  extra: (JsonObjectBuilder) -> Unit = {}
): JsonObject {
  return buildJson {
    if (applyDefaults) {
      it.applyJsonSchemaDefaults(this, annotations, skipNullCheck, skipTypeCheck)
    }

    it.apply(extra)
  }
}

private inline fun buildJson(builder: (JsonObjectBuilder) -> Unit): JsonObject =
  JsonObject(JsonObjectBuilder().apply(builder).content)

private class JsonObjectBuilder(val content: MutableMap<String, JsonElement> = linkedMapOf()) :
  MutableMap<String, JsonElement> by content {
  operator fun set(key: String, value: Iterable<String>) =
    set(key, JsonArray(value.map(::JsonPrimitive)))

  operator fun set(key: String, value: String?) = set(key, JsonPrimitive(value))

  operator fun set(key: String, value: Number?) = set(key, JsonPrimitive(value))
}

private class JsonSchemaDefinitions(private val isEnabled: Boolean = true) {
  private val definitions: MutableMap<String, JsonObject> = mutableMapOf()
  private val creator: MutableMap<String, () -> JsonObject> = mutableMapOf()

  fun getId(key: Key): String {
    val (descriptor, annotations) = key

    return annotations.lastOfInstance<JsonSchema.Definition>()?.id?.takeIf(String::isNotEmpty)
      ?: (descriptor.hashCode().toLong() shl 32 xor annotations.hashCode().toLong())
        .toString(36)
        .replaceFirst("-", "x")
  }

  fun canGenerateDefinitions(key: Key): Boolean {
    return key.annotations.any { it !is JsonSchema.NoDefinition && it is JsonSchema.Definition }
  }

  operator fun contains(key: Key): Boolean = getId(key) in definitions

  operator fun set(key: Key, value: JsonObject) {
    definitions[getId(key)] = value
  }

  operator fun get(key: Key): JsonObject {
    val id = getId(key)

    return key.descriptor.jsonSchemaElement(
      key.annotations,
      skipNullCheck = true,
      skipTypeCheck = true
    ) {
      it["\$ref"] = "#/definitions/$id"
    }
  }

  fun get(key: Key, create: () -> JsonObject): JsonObject {
    if (!isEnabled && !canGenerateDefinitions(key)) return create()

    val id = getId(key)

    if (id !in definitions) {
      creator[id] = create
    }

    return get(key)
  }

  fun getDefinitionsAsJsonObject(): JsonObject {
    while (creator.isNotEmpty()) {
      creator.forEach { (id, create) ->
        definitions[id] = create()
        creator.remove(id)
      }
    }

    return JsonObject(definitions)
  }

  data class Key(val descriptor: SerialDescriptor, val annotations: List<Annotation>)
}
