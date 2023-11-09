/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.models

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * The hyperparameters used for the fine-tuning job. See the
 * [fine-tuning guide](/docs/guides/legacy-fine-tuning/hyperparameters) for more details.
 *
 * @param batchSize The batch size to use for training. The batch size is the number of training
 *   examples used to train a single forward and backward pass.
 * @param learningRateMultiplier The learning rate multiplier to use for training.
 * @param nEpochs The number of epochs to train the model for. An epoch refers to one full cycle
 *   through the training dataset.
 * @param promptLossWeight The weight to use for loss on the prompt tokens.
 * @param classificationNClasses The number of classes to use for computing classification metrics.
 * @param classificationPositiveClass The positive class to use for computing classification
 *   metrics.
 * @param computeClassificationMetrics The classification metrics to compute using the validation
 *   dataset at the end of every epoch.
 */
@Serializable
data class FineTuneHyperparams(

  /* The batch size to use for training. The batch size is the number of training examples used to train a single forward and backward pass.  */
  @SerialName(value = "batch_size") @Required val batchSize: kotlin.Int,

  /* The learning rate multiplier to use for training.  */
  @SerialName(value = "learning_rate_multiplier")
  @Required
  val learningRateMultiplier: kotlin.Double,

  /* The number of epochs to train the model for. An epoch refers to one full cycle through the training dataset.  */
  @SerialName(value = "n_epochs") @Required val nEpochs: kotlin.Int,

  /* The weight to use for loss on the prompt tokens.  */
  @SerialName(value = "prompt_loss_weight") @Required val promptLossWeight: kotlin.Double,

  /* The number of classes to use for computing classification metrics.  */
  @SerialName(value = "classification_n_classes") val classificationNClasses: kotlin.Int? = null,

  /* The positive class to use for computing classification metrics.  */
  @SerialName(value = "classification_positive_class")
  val classificationPositiveClass: kotlin.String? = null,

  /* The classification metrics to compute using the validation dataset at the end of every epoch.  */
  @SerialName(value = "compute_classification_metrics")
  val computeClassificationMetrics: kotlin.Boolean? = null
)
