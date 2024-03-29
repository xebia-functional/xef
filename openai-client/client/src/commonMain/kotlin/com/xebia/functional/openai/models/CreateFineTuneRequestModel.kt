/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.models

import kotlinx.serialization.*

/**
 * The name of the base model to fine-tune. You can select one of \"ada\", \"babbage\", \"curie\",
 * \"davinci\", or a fine-tuned model created after 2022-04-21 and before 2023-08-22. To learn more
 * about these models, see the [Models](/docs/models) documentation.
 *
 * Values: ada,babbage,curie,davinci
 */
@Serializable
enum class CreateFineTuneRequestModel(val value: kotlin.String) {

  @SerialName(value = "ada") ada("ada"),
  @SerialName(value = "babbage") babbage("babbage"),
  @SerialName(value = "curie") curie("curie"),
  @SerialName(value = "davinci") davinci("davinci");

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
      if (data is CreateFineTuneRequestModel) "$data" else null

    /** Returns a valid [CreateFineTuneRequestModel] for [data], null otherwise. */
    fun decode(data: kotlin.Any?): CreateFineTuneRequestModel? =
      data?.let {
        val normalizedData = "$it".lowercase()
        values().firstOrNull { value -> it == value || normalizedData == "$value".lowercase() }
      }
  }
}
