/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The `fine_tuning.job` object represents a fine-tuning job that has been created through the API.
 *
 * @param id The object identifier, which can be referenced in the API endpoints.
 * @param createdAt The Unix timestamp (in seconds) for when the fine-tuning job was created.
 * @param error
 * @param fineTunedModel The name of the fine-tuned model that is being created. The value will be
 *   null if the fine-tuning job is still running.
 * @param finishedAt The Unix timestamp (in seconds) for when the fine-tuning job was finished. The
 *   value will be null if the fine-tuning job is still running.
 * @param hyperparameters
 * @param model The base model that is being fine-tuned.
 * @param `object` The object type, which is always \"fine_tuning.job\".
 * @param organizationId The organization that owns the fine-tuning job.
 * @param resultFiles The compiled results file ID(s) for the fine-tuning job. You can retrieve the
 *   results with the [Files API](/docs/api-reference/files/retrieve-contents).
 * @param status The current status of the fine-tuning job, which can be either `validating_files`,
 *   `queued`, `running`, `succeeded`, `failed`, or `cancelled`.
 * @param trainedTokens The total number of billable tokens processed by this fine-tuning job. The
 *   value will be null if the fine-tuning job is still running.
 * @param trainingFile The file ID used for training. You can retrieve the training data with the
 *   [Files API](/docs/api-reference/files/retrieve-contents).
 * @param validationFile The file ID used for validation. You can retrieve the validation results
 *   with the [Files API](/docs/api-reference/files/retrieve-contents).
 */
@Serializable
data class FineTuningJob(
  /* The object identifier, which can be referenced in the API endpoints. */
  @SerialName(value = "id") val id: kotlin.String,
  /* The Unix timestamp (in seconds) for when the fine-tuning job was created. */
  @SerialName(value = "created_at") val createdAt: kotlin.Int,
  @SerialName(value = "error") val error: FineTuningJobError?,
  /* The name of the fine-tuned model that is being created. The value will be null if the fine-tuning job is still running. */
  @SerialName(value = "fine_tuned_model") val fineTunedModel: kotlin.String?,
  /* The Unix timestamp (in seconds) for when the fine-tuning job was finished. The value will be null if the fine-tuning job is still running. */
  @SerialName(value = "finished_at") val finishedAt: kotlin.Int?,
  @SerialName(value = "hyperparameters") val hyperparameters: FineTuningJobHyperparameters,
  /* The base model that is being fine-tuned. */
  @SerialName(value = "model") val model: kotlin.String,
  /* The object type, which is always \"fine_tuning.job\". */
  @SerialName(value = "object") val `object`: FineTuningJob.`Object`,
  /* The organization that owns the fine-tuning job. */
  @SerialName(value = "organization_id") val organizationId: kotlin.String,
  /* The compiled results file ID(s) for the fine-tuning job. You can retrieve the results with the [Files API](/docs/api-reference/files/retrieve-contents). */
  @SerialName(value = "result_files") val resultFiles: kotlin.collections.List<kotlin.String>,
  /* The current status of the fine-tuning job, which can be either `validating_files`, `queued`, `running`, `succeeded`, `failed`, or `cancelled`. */
  @SerialName(value = "status") val status: FineTuningJob.Status,
  /* The total number of billable tokens processed by this fine-tuning job. The value will be null if the fine-tuning job is still running. */
  @SerialName(value = "trained_tokens") val trainedTokens: kotlin.Int?,
  /* The file ID used for training. You can retrieve the training data with the [Files API](/docs/api-reference/files/retrieve-contents). */
  @SerialName(value = "training_file") val trainingFile: kotlin.String,
  /* The file ID used for validation. You can retrieve the validation results with the [Files API](/docs/api-reference/files/retrieve-contents). */
  @SerialName(value = "validation_file") val validationFile: kotlin.String?
) {

  /**
   * The object type, which is always \"fine_tuning.job\".
   *
   * Values: fine_tuning_job
   */
  @Serializable
  enum class `Object`(val value: kotlin.String) {
    @SerialName(value = "fine_tuning.job") fine_tuning_job("fine_tuning.job")
  }
  /**
   * The current status of the fine-tuning job, which can be either `validating_files`, `queued`,
   * `running`, `succeeded`, `failed`, or `cancelled`.
   *
   * Values: validating_files,queued,running,succeeded,failed,cancelled
   */
  @Serializable
  enum class Status(val value: kotlin.String) {
    @SerialName(value = "validating_files") validating_files("validating_files"),
    @SerialName(value = "queued") queued("queued"),
    @SerialName(value = "running") running("running"),
    @SerialName(value = "succeeded") succeeded("succeeded"),
    @SerialName(value = "failed") failed("failed"),
    @SerialName(value = "cancelled") cancelled("cancelled")
  }
}
