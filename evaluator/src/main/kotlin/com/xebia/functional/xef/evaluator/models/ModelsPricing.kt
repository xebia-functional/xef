package com.xebia.functional.xef.evaluator.models

data class ModelsPricing(
  val modelName: String,
  val currency: String,
  val input: ModelsPricingItem,
  val output: ModelsPricingItem
) {

  companion object {

    const val oneMillion = 1_000_000
    val oneThousand = 1_000

    // The pricing for the models was updated the May 2st, 2024
    // Be sure to update the pricing for each model

    val gpt4Turbo =
      ModelsPricing(
        modelName = "gpt-4-turbo",
        currency = "USD",
        input = ModelsPricingItem(10.0, oneMillion),
        output = ModelsPricingItem(30.0, oneMillion)
      )

    val gpt4 =
      ModelsPricing(
        modelName = "gpt-4-turbo",
        currency = "USD",
        input = ModelsPricingItem(30.0, oneMillion),
        output = ModelsPricingItem(60.0, oneMillion)
      )

    val gpt3_5Turbo =
      ModelsPricing(
        modelName = "gpt-3.5-turbo",
        currency = "USD",
        input = ModelsPricingItem(0.5, oneMillion),
        output = ModelsPricingItem(1.5, oneMillion)
      )
  }
}

data class ModelsPricingItem(val price: Double, val perTokens: Int)
