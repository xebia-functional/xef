@file:OptIn(ExperimentalSerializationApi::class)

package com.xebia.functional.auto.serialization

/*
Ported over from https://github.com/Ricky12Awesome/json-schema-serialization
which states the following:

> I lost interest in this project, that why there hasn't been any changes for
> over 2 years if anyone wants to maintain this project or a fork of this project, then I will update this readme

// TODO: We should consider a fork and maintain it ourselves.
 */
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Represents the type of json type
 */
enum class JsonType(jsonType: String) {
  /**
   * Represents the json array type
   */
  ARRAY("array"),

  /**
   * Represents the json number type
   */
  NUMBER("number"),

  /**
   * Represents the string type
   */
  STRING("string"),

  /**
   * Represents the boolean type
   */
  BOOLEAN("boolean"),

  /**
   * Represents the object type, this is used for serializing normal classes
   */
  OBJECT("object"),

  /**
   * Represents the object type, this is used for serializing sealed classes
   */
  OBJECT_SEALED("object"),

  /**
   * Represents the object type, this is used for serializing maps
   */
  OBJECT_MAP("object");

  val json = JsonPrimitive(jsonType)

  override fun toString(): String = json.content
}

@Target()
annotation class JsonSchema {
  /**
   * Description of this property
   */
  @SerialInfo
  @Repeatable
  @Retention(AnnotationRetention.BINARY)
  @Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
  annotation class Description(val lines: Array<out String>)

  /**
   * Enum-like values for non-enum string
   */
  @SerialInfo
  @Retention(AnnotationRetention.BINARY)
  @Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
  annotation class StringEnum(val values: Array<out String>)

  /**
   * Minimum and Maximum values using whole numbers
   *
   * Only works when [SerialKind] is any of
   * [PrimitiveKind.BYTE], [PrimitiveKind.SHORT], [PrimitiveKind.INT], [PrimitiveKind.LONG]
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

  /**
   * This property will not create definitions
   */
  @SerialInfo
  @Retention(AnnotationRetention.BINARY)
  @Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
  annotation class NoDefinition
}

/**
 * Adds a `$schema` property with the provided [url] that points to the Json Schema,
 * this can be a File location or a HTTP URL
 *
 * This is so when you serialize your [value] it will use [url] as it's Json Schema for code completion.
 */
fun <T> Json.encodeWithSchema(serializer: SerializationStrategy<T>, value: T, url: String): String {
  val json = encodeToJsonElement(serializer, value) as JsonObject
  val append = mapOf("\$schema" to JsonPrimitive(url))

  return encodeToString(JsonObject.serializer(), JsonObject(append + json))
}

/**
 * Stringifies the provided [descriptor] with [buildJsonSchema]
 *
 * @param generateDefinitions Should this generate definitions by default
 */
fun Json.encodeToSchema(descriptor: SerialDescriptor, generateDefinitions: Boolean = true): String {
  return encodeToString(JsonObject.serializer(), buildJsonSchema(descriptor, generateDefinitions))
}

/**
 * Stringifies the provided [serializer] with [buildJsonSchema], same as doing
 * ```kotlin
 * json.encodeToSchema(serializer.descriptor)
 * ```
 * @param generateDefinitions Should this generate definitions by default
 */
fun Json.encodeToSchema(serializer: SerializationStrategy<*>, generateDefinitions: Boolean = true): String {
  return encodeToSchema(serializer.descriptor, generateDefinitions)
}

/**
 * Creates a Json Schema using the provided [descriptor]
 *
 * @param autoDefinitions automatically generate definitions by default
 */
fun buildJsonSchema(descriptor: SerialDescriptor, autoDefinitions: Boolean = false): JsonObject {
  val prepend = mapOf("\$schema" to JsonPrimitive("http://json-schema.org/draft-07/schema"))
  val definitions = JsonSchemaDefinitions(autoDefinitions)
  val root = descriptor.createJsonSchema(descriptor.annotations, definitions)
  val append = mapOf("definitions" to definitions.getDefinitionsAsJsonObject())

  return JsonObject(prepend + root + append)
}

/**
 * Creates a Json Schema using the provided [serializer],
 * same as doing `jsonSchema(serializer.descriptor)`
 *
 * @param generateDefinitions Should this generate definitions by default
 */
fun buildJsonSchema(serializer: SerializationStrategy<*>, generateDefinitions: Boolean = true): JsonObject {
  return buildJsonSchema(serializer.descriptor, generateDefinitions)
}

@PublishedApi
internal inline val SerialDescriptor.jsonLiteral
  inline get() = kind.jsonType.json

@PublishedApi
internal val SerialKind.jsonType: JsonType
  get() = when (this) {
    StructureKind.LIST -> JsonType.ARRAY
    StructureKind.MAP -> JsonType.OBJECT_MAP
    PolymorphicKind.SEALED -> JsonType.OBJECT_SEALED
    PrimitiveKind.BYTE, PrimitiveKind.SHORT, PrimitiveKind.INT, PrimitiveKind.LONG, PrimitiveKind.FLOAT, PrimitiveKind.DOUBLE -> JsonType.NUMBER

    PrimitiveKind.STRING, PrimitiveKind.CHAR, SerialKind.ENUM -> JsonType.STRING
    PrimitiveKind.BOOLEAN -> JsonType.BOOLEAN
    else -> JsonType.OBJECT
  }

internal inline fun <reified T> List<Annotation>.lastOfInstance(): T? {
  return filterIsInstance<T>().lastOrNull()
}

@PublishedApi
internal fun SerialDescriptor.jsonSchemaObject(definitions: JsonSchemaDefinitions): JsonObject {
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

internal fun SerialDescriptor.jsonSchemaObjectMap(definitions: JsonSchemaDefinitions): JsonObject {
  return jsonSchemaElement(annotations, skipNullCheck = false) {
    val (key, value) = elementDescriptors.toList()

    require(key.kind == PrimitiveKind.STRING) {
      "cannot have non string keys in maps"
    }

    it["additionalProperties"] = value.createJsonSchema(getElementAnnotations(1), definitions)
  }
}

@PublishedApi
internal fun SerialDescriptor.jsonSchemaObjectSealed(definitions: JsonSchemaDefinitions): JsonObject {
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
    anyOf += buildJson { nullable ->
      nullable["type"] = "null"
    }
  }

  value.elementDescriptors.forEachIndexed { index, child ->
    val schema = child.createJsonSchema(value.getElementAnnotations(index), definitions)
    val newSchema = schema.mapValues { (name, element) ->
      if (element is JsonObject && name == "properties") {
        val prependProps = mutableMapOf<String, JsonElement>()

        prependProps["type"] = buildJson {
          it["const"] = child.serialName
        }

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

@PublishedApi
internal fun SerialDescriptor.jsonSchemaArray(
  annotations: List<Annotation> = listOf(), definitions: JsonSchemaDefinitions
): JsonObject {
  return jsonSchemaElement(annotations) {
    val type = getElementDescriptor(0)

    it["items"] = type.createJsonSchema(getElementAnnotations(0), definitions)
  }
}

@PublishedApi
internal fun SerialDescriptor.jsonSchemaString(
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

@PublishedApi
internal fun SerialDescriptor.jsonSchemaNumber(
  annotations: List<Annotation> = listOf()
): JsonObject {
  return jsonSchemaElement(annotations) {
    val value = when (kind) {
      PrimitiveKind.FLOAT, PrimitiveKind.DOUBLE -> annotations.lastOfInstance<JsonSchema.FloatRange>()
        ?.let { it.min as Number to it.max as Number }

      PrimitiveKind.BYTE, PrimitiveKind.SHORT, PrimitiveKind.INT, PrimitiveKind.LONG -> annotations.lastOfInstance<JsonSchema.IntRange>()
        ?.let { it.min as Number to it.max as Number }

      else -> error("$kind is not a Number")
    }

    value?.let { (min, max) ->
      it["minimum"] = min
      it["maximum"] = max
    }
  }
}

@PublishedApi
internal fun SerialDescriptor.jsonSchemaBoolean(
  annotations: List<Annotation> = listOf()
): JsonObject {
  return jsonSchemaElement(annotations)
}

@PublishedApi
internal fun SerialDescriptor.createJsonSchema(
  annotations: List<Annotation>, definitions: JsonSchemaDefinitions
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

@PublishedApi
internal fun JsonObjectBuilder.applyJsonSchemaDefaults(
  descriptor: SerialDescriptor,
  annotations: List<Annotation>,
  skipNullCheck: Boolean = false,
  skipTypeCheck: Boolean = false
) {
  if (descriptor.isNullable && !skipNullCheck) {
    this["if"] = buildJson {
      it["type"] = descriptor.jsonLiteral
    }
    this["else"] = buildJson {
      it["type"] = "null"
    }
  } else {
    if (!skipTypeCheck) {
      this["type"] = descriptor.jsonLiteral
    }
  }

  if (descriptor.kind == SerialKind.ENUM) {
    this["enum"] = descriptor.elementNames
  }

  if (annotations.isNotEmpty()) {
    val description = annotations.filterIsInstance<JsonSchema.Description>().joinToString("\n") {
      it.lines.joinToString("\n")
    }

    if (description.isNotEmpty()) {
      this["description"] = description
    }
  }
}

internal inline fun SerialDescriptor.jsonSchemaElement(
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

internal inline fun buildJson(builder: (JsonObjectBuilder) -> Unit): JsonObject {
  return JsonObject(JsonObjectBuilder().apply(builder).content)
}

internal class JsonObjectBuilder(
  val content: MutableMap<String, JsonElement> = linkedMapOf()
) : MutableMap<String, JsonElement> by content {
  operator fun set(key: String, value: Iterable<String>) = set(key, JsonArray(value.map(::JsonPrimitive)))
  operator fun set(key: String, value: String?) = set(key, JsonPrimitive(value))
  operator fun set(key: String, value: Number?) = set(key, JsonPrimitive(value))
}

internal class JsonSchemaDefinitions(private val isEnabled: Boolean = true) {
  private val definitions: MutableMap<String, JsonObject> = mutableMapOf()
  private val creator: MutableMap<String, () -> JsonObject> = mutableMapOf()

  fun getId(key: Key): String {
    val (descriptor, annotations) = key

    return annotations.lastOfInstance<JsonSchema.Definition>()?.id?.takeIf(String::isNotEmpty)
      ?: (descriptor.hashCode().toLong() shl 32 xor annotations.hashCode().toLong()).toString(36)
        .replaceFirst("-", "x")
  }

  fun canGenerateDefinitions(key: Key): Boolean {
    return key.annotations.any {
      it !is JsonSchema.NoDefinition && it is JsonSchema.Definition
    }
  }

  operator fun contains(key: Key): Boolean = getId(key) in definitions

  operator fun set(key: Key, value: JsonObject) {
    definitions[getId(key)] = value
  }

  operator fun get(key: Key): JsonObject {
    val id = getId(key)

    return key.descriptor.jsonSchemaElement(key.annotations, skipNullCheck = true, skipTypeCheck = true) {
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
