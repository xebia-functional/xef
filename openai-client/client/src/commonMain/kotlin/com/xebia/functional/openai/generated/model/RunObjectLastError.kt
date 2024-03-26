/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The last error associated with this run. Will be `null` if there are no errors.
 *
 * @param code One of `server_error` or `rate_limit_exceeded`.
 * @param message A human-readable description of the error.
 */
@Serializable
data class RunObjectLastError(
  /* One of `server_error` or `rate_limit_exceeded`. */
  @SerialName(value = "code") val code: RunObjectLastError.Code,
  /* A human-readable description of the error. */
  @SerialName(value = "message") val message: kotlin.String
) {

  /**
   * One of `server_error` or `rate_limit_exceeded`.
   *
   * Values: server_error,rate_limit_exceeded
   */
  @Serializable
  enum class Code(name: kotlin.String) {
    @SerialName(value = "server_error") server_error("server_error"),
    @SerialName(value = "rate_limit_exceeded") rate_limit_exceeded("rate_limit_exceeded")
  }
}
