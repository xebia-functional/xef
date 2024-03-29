/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.models

import kotlinx.serialization.*

/**
 * The model to use for image generation.
 *
 * Values: dall_e_2,dall_e_3
 */
@Serializable
enum class CreateImageRequestModel(val value: kotlin.String) {

  @SerialName(value = "dall-e-2") dall_e_2("dall-e-2"),
  @SerialName(value = "dall-e-3") dall_e_3("dall-e-3");

  /**
   * Override [toString()] to avoid using the enum variable name as the value, and instead use the
   * actual value defined in the API spec file.
   *
   * This solves a problem when the variable name and its value are different, and ensures that the
   * client sends the correct enum values to the server always.
   */
  override fun toString(): kotlin.String = value

  companion object {
    /** Converts the provided [data] to a [String] on success, null otherwise. */
    fun encode(data: kotlin.Any?): kotlin.String? =
      if (data is CreateImageRequestModel) "$data" else null

    /** Returns a valid [CreateImageRequestModel] for [data], null otherwise. */
    fun decode(data: kotlin.Any?): CreateImageRequestModel? =
      data?.let {
        val normalizedData = "$it".lowercase()
        values().firstOrNull { value -> it == value || normalizedData == "$value".lowercase() }
      }
  }
}
