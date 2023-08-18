package com.xebia.functional.xef.auto.expressions

import com.xebia.functional.xef.auto.Description
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.lang.Infer
import kotlinx.serialization.Serializable

enum class Cuisine {
  Italian,
  Indian,
  Chinese,
  Mediterranean,
  Vegan,
  Keto,
  Paleo,
  Infer
}

enum class MainIngredient {
  Chicken,
  Beef,
  Tofu,
  Mushroom,
  Lentils,
  Shrimp,
  Infer
}

enum class CookingMethod {
  Bake,
  Fry,
  Steam,
  Saute,
  Grill,
  SlowCook,
  Infer
}

@Serializable
data class Ingredient(val name: String, val quantity: String, val unit: String? = null)

@Serializable
data class RecipeState(
  val title: String,
  val cuisine: Cuisine,
  val mainIngredient: MainIngredient,
  val cookingMethod: CookingMethod,
  val ingredients: List<Ingredient>,
  val description: String,
  val steps: List<String>,
  val totalTime: Int
)

@Serializable
data class DietaryConstraints(
  val allergens: List<String>,
  val dislikedIngredients: List<String>,
  val calorieLimit: Int
)

@Serializable
data class GenerateRecipe(val state: RecipeState, val constraints: DietaryConstraints)

@Serializable
data class RecipePrompt(
  @Description(
    "Generate a detailed and mouthwatering recipe." +
      "Make sure to use appropriate culinary terms." +
      "Recipe should be easy to follow for a beginner."
  )
  val title: String,
  val ingredients: List<Ingredient>,
  val prepTime: String, // in minutes
  val cookTime: String, // in minutes
  val servings: Int,
  val steps: List<String>,
  val notes: String? = null
)

suspend fun main() {
  OpenAI.conversation {
    val infer = Infer(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, conversation)
    val recipe: RecipePrompt =
      infer(
        Prompt(
          """
                    Assume the role of a world-class chef. Your task is to create unique and delicious recipes tailored 
                    to specific dietary constraints and preferences using the inputs provided.
                    """
            .trimIndent()
        )
      ) {
        GenerateRecipe(
          state =
            RecipeState(
              title = inferString,
              cuisine = Cuisine.Mediterranean,
              mainIngredient = MainIngredient.Chicken,
              cookingMethod = CookingMethod.Grill,
              ingredients =
                listOf(
                  Ingredient(name = inferString, quantity = inferString, unit = inferString),
                  Ingredient(name = inferString, quantity = inferString, unit = inferString)
                ),
              description = inferString,
              steps = listOf(inferString, inferString, inferString),
              totalTime = inferInt
            ),
          constraints =
            DietaryConstraints(
              allergens = listOf("nuts", "shellfish"),
              dislikedIngredients = listOf("brussels sprouts"),
              calorieLimit = 600
            )
        )
      }

    println(recipe)
  }
}
