package com.xebia.functional.xef.auto.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.victools.jsonschema.generator.OptionPreset
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationModule
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationOption

object JacksonSerialization {
  val module: JakartaValidationModule =
    JakartaValidationModule(
      JakartaValidationOption.NOT_NULLABLE_FIELD_IS_REQUIRED,
      JakartaValidationOption.INCLUDE_PATTERN_EXPRESSIONS
    )

  val configBuilder: SchemaGeneratorConfigBuilder =
    SchemaGeneratorConfigBuilder(
        com.github.victools.jsonschema.generator.SchemaVersion.DRAFT_7,
        OptionPreset.PLAIN_JSON
      )
      .with(module)

  val config: SchemaGeneratorConfig = configBuilder.build()

  val schemaGenerator = SchemaGenerator(config)

  val objectMapper = ObjectMapper()
}
