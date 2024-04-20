@file:OptIn(ExperimentalSerializationApi::class)

package com.xebia.functional.xef.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.*

class JavaSerialDescriptor(val clazz: Class<*>) : SerialDescriptor {
  override val serialName: String = clazz.name
  override val kind: SerialKind =
    // extract from the class info to build the SerialKind
    when {
      clazz.isEnum -> SerialKind.ENUM
      clazz.isPrimitive ->
        when (clazz) {
          Boolean::class.java -> PrimitiveKind.BOOLEAN
          Byte::class.java -> PrimitiveKind.BYTE
          Char::class.java -> PrimitiveKind.CHAR
          Double::class.java -> PrimitiveKind.DOUBLE
          Float::class.java -> PrimitiveKind.FLOAT
          Int::class.java -> PrimitiveKind.INT
          Long::class.java -> PrimitiveKind.LONG
          Short::class.java -> PrimitiveKind.SHORT
          else -> error("Unknown primitive type $clazz")
        }
      clazz.isArray -> StructureKind.LIST
      else -> {
        when (clazz) {
          String::class.java -> PrimitiveKind.STRING
          else -> StructureKind.CLASS
        }
      }
    }
  override val elementsCount: Int = clazz.declaredFields.size
  override fun getElementIndex(name: String): Int = clazz.declaredFields.indexOfFirst { it.name == name }
  override fun getElementName(index: Int): String = clazz.declaredFields[index].name
  override fun getElementDescriptor(index: Int): SerialDescriptor = JavaSerialDescriptor(clazz.declaredFields[index].type)
  override fun isElementOptional(index: Int): Boolean = false

  @ExperimentalSerializationApi
  override fun getElementAnnotations(index: Int): List<Annotation> {
    return clazz.declaredFields[index].annotations.toList()
  }
}
