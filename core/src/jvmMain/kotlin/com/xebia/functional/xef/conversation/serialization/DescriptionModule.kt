package com.xebia.functional.xef.conversation.serialization

import com.github.victools.jsonschema.generator.MemberScope
import com.github.victools.jsonschema.generator.Module
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.TypeScope
import com.xebia.functional.xef.conversation.jvm.Description

internal class DescriptionModule : Module {
  override fun applyToConfigBuilder(builder: SchemaGeneratorConfigBuilder) {
    builder.forTypesInGeneral().withDescriptionResolver(::resolveDescriptionForType)
    builder.forFields().withDescriptionResolver(::resolveDescription)
    builder.forMethods().withDescriptionResolver(::resolveDescription)
  }

  protected fun resolveDescription(member: MemberScope<*, *>?): String? {
    return member?.getAnnotation(Description::class.java)?.value
  }

  protected fun resolveDescriptionForType(scope: TypeScope?): String? {
    return scope
      ?.type
      ?.erasedType
      ?.annotations
      ?.filterIsInstance<Description>()
      ?.map { it.value }
      ?.firstOrNull()
  }
}
