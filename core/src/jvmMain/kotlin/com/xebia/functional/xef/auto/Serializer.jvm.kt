package com.xebia.functional.xef.auto

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.victools.jsonschema.generator.OptionPreset
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationModule
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationOption

private val schemaGenerator: SchemaGenerator by lazy {
  val module = JakartaValidationModule(
    JakartaValidationOption.NOT_NULLABLE_FIELD_IS_REQUIRED,
    JakartaValidationOption.INCLUDE_PATTERN_EXPRESSIONS
  )
  val configBuilder: SchemaGeneratorConfigBuilder = SchemaGeneratorConfigBuilder(
    com.github.victools.jsonschema.generator.SchemaVersion.DRAFT_7,
    OptionPreset.PLAIN_JSON
  )
    .with(module)
  val config: SchemaGeneratorConfig = configBuilder.build()
  val schemaGenerator = SchemaGenerator(config)
  schemaGenerator
}

private val objectMapper: ObjectMapper by lazy {
  ObjectMapper()
}

@Throws(JsonProcessingException::class)
fun <A> serialize(target: Class<A>, json: String): A {
  return try {
    objectMapper.readValue(json, target)
  } catch (e: JsonProcessingException) {
    throw RuntimeException(e)
  }
}

fun <A> encodeJsonSchema(target: Class<A>): String {
  return schemaGenerator.generateSchema(target).toString()
}
