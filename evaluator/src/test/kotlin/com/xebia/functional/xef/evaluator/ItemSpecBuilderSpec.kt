package com.xebia.functional.xef.evaluator

import arrow.core.raise.either
import com.xebia.functional.xef.evaluator.models.ItemSpec
import com.xebia.functional.xef.evaluator.models.errors.EmptyContextDescription
import com.xebia.functional.xef.evaluator.models.errors.EmptyItemSpecInput
import com.xebia.functional.xef.evaluator.models.errors.EmptyOutputResponse
import com.xebia.functional.xef.evaluator.models.errors.ValidationError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ItemSpecBuilderSpec {

  @Test
  fun shouldBuildAnItemSpec() = runTest {
    val inputItem = "Please provide a movie title, genre and director"
    val myContextDescription = "Contains information about a movie"
    val firstOutputResponse =
      """
        | Movie Title: The Pursuit of Dreams
        | Genre: Drama
        | Director: Christopher Nolan
        """
        .trimIndent()
    val secondOutputResponse = "I don't know"

    val itemSpec =
      ItemSpec(inputItem) {
        contextDescription { myContextDescription }
        outputResponse { firstOutputResponse }
        outputResponse { secondOutputResponse }
      }

    either {
      val specItem = itemSpec.bind()
      specItem.input shouldBe inputItem
      specItem.context shouldBe listOf(myContextDescription)
      specItem.outputs shouldContainAll listOf(firstOutputResponse, secondOutputResponse)
    }
  }

  @Test
  fun shouldInvalidWhenInputIsEmpty() = runTest {
    val inputItem = ""
    val myContextDescription = "Contains information about a movie"
    val firstOutputResponse =
      """
        | Movie Title: The Pursuit of Dreams
        | Genre: Drama
        | Director: Christopher Nolan
        """
        .trimIndent()
    val secondOutputResponse = "I don't know"

    val invalidItemSpec =
      ItemSpec(inputItem) {
        contextDescription { myContextDescription }
        outputResponse { firstOutputResponse }
        outputResponse { secondOutputResponse }
      }

    invalidItemSpec shouldBeLeft listOf<ValidationError>(EmptyItemSpecInput)
  }

  @Test
  fun shouldInvalidWhenContextDescriptionIsEmpty() = runTest {
    val inputItem = "Please provide a movie title, genre and director"
    val myContextDescription = "      "
    val firstOutputResponse =
      """
        | Movie Title: The Pursuit of Dreams
        | Genre: Drama
        | Director: Christopher Nolan
        """
        .trimIndent()
    val secondOutputResponse = "I don't know"

    val invalidItemSpec =
      ItemSpec(inputItem) {
        contextDescription { myContextDescription }
        outputResponse { firstOutputResponse }
        outputResponse { secondOutputResponse }
      }

    invalidItemSpec shouldBeLeft listOf<ValidationError>(EmptyContextDescription)
  }

  @Test
  fun shouldInvalidWhenFirstOutputResponseIsEmpty() = runTest {
    val inputItem = "Please provide a movie title, genre and director"
    val myContextDescription = "Contains information about a movie"
    val firstOutputResponse = "   "
    val secondOutputResponse = "I don't know"

    val invalidItemSpec =
      ItemSpec(inputItem) {
        contextDescription { myContextDescription }
        outputResponse { firstOutputResponse }
        outputResponse { secondOutputResponse }
      }

    invalidItemSpec shouldBeLeft listOf<ValidationError>(EmptyOutputResponse)
  }

  @Test
  fun shouldInvalidWhenSecondOutputResponseIsEmpty() = runTest {
    val inputItem = "Please provide a movie title, genre and director"
    val myContextDescription = "Contains information about a movie"
    val firstOutputResponse =
      """
        | Movie Title: The Pursuit of Dreams
        | Genre: Drama
        | Director: Christopher Nolan
        """
        .trimIndent()
    val secondOutputResponse = " "

    val invalidItemSpec =
      ItemSpec(inputItem) {
        contextDescription { myContextDescription }
        outputResponse { firstOutputResponse }
        outputResponse { secondOutputResponse }
      }

    invalidItemSpec shouldBeLeft listOf<ValidationError>(EmptyOutputResponse)
  }

  @Test
  fun shouldInvalidWhenAllOutputsResponseAreEmpty() = runTest {
    val inputItem = "Please provide a movie title, genre and director"
    val myContextDescription = "Contains information about a movie"
    val firstOutputResponse = " "
    val secondOutputResponse = " "

    val invalidItemSpec =
      ItemSpec(inputItem) {
        contextDescription { myContextDescription }
        outputResponse { firstOutputResponse }
        outputResponse { secondOutputResponse }
      }

    invalidItemSpec shouldBeLeft listOf<ValidationError>(EmptyOutputResponse, EmptyOutputResponse)
  }

  @Test
  fun shouldInvalidWhenAnyCombinationOfInputAreEmpty() = runTest {
    val inputItem = " "
    val myContextDescription = ""
    val firstOutputResponse = " "
    val secondOutputResponse = "  "

    val invalidItemSpec =
      ItemSpec(inputItem) {
        contextDescription { myContextDescription }
        outputResponse { firstOutputResponse }
        outputResponse { secondOutputResponse }
      }

    invalidItemSpec shouldBeLeft
      listOf(EmptyItemSpecInput, EmptyContextDescription, EmptyOutputResponse, EmptyOutputResponse)
  }
}
