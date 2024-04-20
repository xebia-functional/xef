@file:OptIn(ExperimentalSerializationApi::class)

package com.xebia.functional.xef.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialKind

interface Serializer<T> {

    fun serialize(value: T): String
    fun deserialize(value: String): T
    val name: String
    val schema: String
    val kind : SerialKind
    val isNullable: Boolean

    val elementsCount: Int

    val isFlowOfString : Boolean

    val isFlowOfFunction : Boolean

    val flowOfFunctionSerializer : Serializer<*>?

    fun getElementName(index: Int): String = elementNames()[index]

    fun getElementAnnotations(index: Int): List<Annotation> = elements()[index].annotations()

    fun isElementOptional(index: Int): Boolean = elements()[index].isNullable

    fun annotations(): List<Annotation>

    fun elements(): List<Serializer<*>>

    fun elementNames(): List<String>

    fun cases(): List<Serializer<*>>

    fun getElementDescriptor(index: Int): Serializer<*> = elements()[index]

    val elementDescriptors: Iterable<Serializer<*>>
    get() = Iterable {
        object : Iterator<Serializer<*>> {
            private var elementsLeft = elementsCount
            override fun hasNext(): Boolean = elementsLeft > 0

            override fun next(): Serializer<*> {
                return getElementDescriptor(elementsCount - (elementsLeft--))
            }
        }
    }
}


