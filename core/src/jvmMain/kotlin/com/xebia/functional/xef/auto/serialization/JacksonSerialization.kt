package com.xebia.functional.xef.auto.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.victools.jsonschema.generator.*
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationModule
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationOption

object JacksonSerialization {
  val module: JakartaValidationModule =
    JakartaValidationModule(
      JakartaValidationOption.NOT_NULLABLE_FIELD_IS_REQUIRED,
      JakartaValidationOption.INCLUDE_PATTERN_EXPRESSIONS
    )

  val configBuilder: SchemaGeneratorConfigBuilder =
    SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
      .with(
        Option.FIELDS_DERIVED_FROM_ARGUMENTFREE_METHODS,
        Option.NONSTATIC_NONVOID_NONGETTER_METHODS
      )
      .with(module)
      .with(DescriptionModule())

  val config: SchemaGeneratorConfig = configBuilder.build()

  val schemaGenerator = SchemaGenerator(config)

  val objectMapper = ObjectMapper()
}
