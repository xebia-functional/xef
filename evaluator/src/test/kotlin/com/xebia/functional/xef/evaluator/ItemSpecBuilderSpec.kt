package com.xebia.functional.xef.evaluator

import arrow.core.raise.either
import com.xebia.funcional.xef.evaluator.models.ItemSpec
import com.xebia.funcional.xef.evaluator.models.errors.EmptyContextDescription
import com.xebia.funcional.xef.evaluator.models.errors.EmptyOutputResponse
import com.xebia.funcional.xef.evaluator.models.errors.EmptyItemSpecInput
import com.xebia.funcional.xef.evaluator.models.errors.ValidationError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe

class ItemSpecBuilderSpec : ShouldSpec({

  should("build a valid ItemSpec") {
    val inputItem = "Please provide a movie title, genre and director"
    val myContextDescription = "Contains information about a movie"
    val firstOutputResponse = """
        | Movie Title: The Pursuit of Dreams
        | Genre: Drama
        | Director: Christopher Nolan
        """.trimIndent()
    val secondOutputResponse = "I don't know"

    val itemSpec = ItemSpec(inputItem) {
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

  should("Invalid: when input is empty") {
    val inputItem = ""
    val myContextDescription = "Contains information about a movie"
    val firstOutputResponse = """
        | Movie Title: The Pursuit of Dreams
        | Genre: Drama
        | Director: Christopher Nolan
        """.trimIndent()
    val secondOutputResponse = "I don't know"

    val invalidItemSpec = ItemSpec(inputItem) {
      contextDescription { myContextDescription }
      outputResponse { firstOutputResponse }
      outputResponse { secondOutputResponse }
    }

    invalidItemSpec shouldBeLeft listOf<ValidationError>(EmptyItemSpecInput)
  }

  should("Invalid: when context description is empty") {
    val inputItem = "Please provide a movie title, genre and director"
    val myContextDescription = "      "
    val firstOutputResponse = """
        | Movie Title: The Pursuit of Dreams
        | Genre: Drama
        | Director: Christopher Nolan
        """.trimIndent()
    val secondOutputResponse = "I don't know"

    val invalidItemSpec = ItemSpec(inputItem) {
      contextDescription { myContextDescription }
      outputResponse { firstOutputResponse }
      outputResponse { secondOutputResponse }
    }

    invalidItemSpec shouldBeLeft listOf<ValidationError>(EmptyContextDescription)
  }

  should("Invalid: when first output response is empty") {
    val inputItem = "Please provide a movie title, genre and director"
    val myContextDescription = "Contains information about a movie"
    val firstOutputResponse = "   "
    val secondOutputResponse = "I don't know"

    val invalidItemSpec = ItemSpec(inputItem) {
      contextDescription { myContextDescription }
      outputResponse { firstOutputResponse }
      outputResponse { secondOutputResponse }
    }

    invalidItemSpec shouldBeLeft listOf<ValidationError>(EmptyOutputResponse)
  }

  should("Invalid: when second output response is empty") {
    val inputItem = "Please provide a movie title, genre and director"
    val myContextDescription = "Contains information about a movie"
    val firstOutputResponse = """
        | Movie Title: The Pursuit of Dreams
        | Genre: Drama
        | Director: Christopher Nolan
        """.trimIndent()
    val secondOutputResponse = " "

    val invalidItemSpec = ItemSpec(inputItem) {
      contextDescription { myContextDescription }
      outputResponse { firstOutputResponse }
      outputResponse { secondOutputResponse }
    }

    invalidItemSpec shouldBeLeft listOf<ValidationError>(EmptyOutputResponse)
  }

  should("Invalid: when all outputs response are empty") {
    val inputItem = "Please provide a movie title, genre and director"
    val myContextDescription = "Contains information about a movie"
    val firstOutputResponse = " "
    val secondOutputResponse = " "

    val invalidItemSpec = ItemSpec(inputItem) {
      contextDescription { myContextDescription }
      outputResponse { firstOutputResponse }
      outputResponse { secondOutputResponse }
    }

    invalidItemSpec shouldBeLeft listOf<ValidationError>(EmptyOutputResponse, EmptyOutputResponse)
  }

  should("Invalid: when any combination of input, context, or outputs response are empty") {
    val inputItem = " "
    val myContextDescription = ""
    val firstOutputResponse = " "
    val secondOutputResponse = "  "

    val invalidItemSpec = ItemSpec(inputItem) {
      contextDescription { myContextDescription }
      outputResponse { firstOutputResponse }
      outputResponse { secondOutputResponse }
    }

    invalidItemSpec shouldBeLeft listOf(
      EmptyItemSpecInput,
      EmptyContextDescription,
      EmptyOutputResponse,
      EmptyOutputResponse
    )
  }
})
