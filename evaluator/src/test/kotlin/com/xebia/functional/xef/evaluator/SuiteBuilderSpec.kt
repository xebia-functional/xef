package com.xebia.functional.xef.evaluator

import com.xebia.functional.xef.evaluator.models.SuiteSpec
import com.xebia.functional.xef.evaluator.models.errors.EmptyItemSpecOutputResponse
import com.xebia.functional.xef.evaluator.models.errors.EmptySuiteSpecDescription
import com.xebia.functional.xef.evaluator.models.errors.EmptySuiteSpecOutputDescription
import com.xebia.functional.xef.evaluator.models.errors.InvalidNumberOfItemSpecOutputResponse
import com.xebia.functional.xef.evaluator.models.errors.LessOutputsDescriptionThanItemsSpec
import com.xebia.functional.xef.evaluator.models.errors.MoreOutputsDescriptionThanItemsSpec
import com.xebia.functional.xef.evaluator.models.errors.OutputsDescriptionNotProvided
import com.xebia.functional.xef.evaluator.models.errors.ValidationError
import com.xebia.functional.xef.evaluator.utils.Generators.emptyString
import com.xebia.functional.xef.evaluator.utils.Generators.simpleString
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.next
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class SuiteBuilderSpec {
  @Test
  fun shouldBuildValidSuiteSpec() = runTest {
    SuiteSpec(simpleString.next()) {
        outputDescription { simpleString.next() }
        outputDescription { simpleString.next() }

        itemSpec(simpleString.next()) {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
          outputResponse { simpleString.next() }
        }

        itemSpec(simpleString.next()) {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
          outputResponse { simpleString.next() }
        }
      }
      .shouldBeRight()
  }

  @Test
  fun shouldInvalidWithEmptyDescription() = runTest {
    val emptyDescription = emptyString.next()

    SuiteSpec(emptyDescription) {
        outputDescription { simpleString.next() }

        itemSpec(simpleString.next()) {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
        }
      }
      .shouldBeLeft() shouldBe listOf<ValidationError>(EmptySuiteSpecDescription)
  }

  @Test
  fun shouldInvalidWithoutOutputDescriptionProvided() = runTest {
    SuiteSpec(simpleString.next()) {
        // no output description provided ...
        itemSpec(simpleString.next()) {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
          outputResponse { simpleString.next() }
        }
      }
      .shouldBeLeft() shouldBe listOf(OutputsDescriptionNotProvided)
  }

  @Test
  fun shouldInvalidWithMoreOutputsDescriptionsThatItemsSpec() = runTest {
    SuiteSpec(simpleString.next()) {
        outputDescription { simpleString.next() }
        outputDescription { simpleString.next() }

        itemSpec("Please provide a movie title") {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
          outputResponse { simpleString.next() }
        }
      }
      .shouldBeLeft() shouldBe listOf(MoreOutputsDescriptionThanItemsSpec)
  }

  @Test
  fun shouldInvalidWithLessOutputsDescriptionThanItemsSpec() = runTest {
    SuiteSpec(simpleString.next()) {
        outputDescription { simpleString.next() }

        itemSpec(simpleString.next()) {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
          outputResponse { simpleString.next() }
        }

        itemSpec(simpleString.next()) {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
          outputResponse { simpleString.next() }
        }
      }
      .shouldBeLeft() shouldBe listOf(LessOutputsDescriptionThanItemsSpec)
  }

  @Test
  fun shouldInvalidWithEmptyOutputDescriptionAtIndexCeroAndOne() = runTest {
    val emptyOutputDescription = emptyString.next()

    SuiteSpec(simpleString.next()) {
        outputDescription { emptyOutputDescription }
        outputDescription { emptyOutputDescription }
        outputDescription { simpleString.next() }

        itemSpec(simpleString.next()) {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
        }

        itemSpec(simpleString.next()) {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
        }

        itemSpec(simpleString.next()) {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
        }
      }
      .shouldBeLeft() shouldBe
      listOf(
        EmptySuiteSpecOutputDescription(0),
        EmptySuiteSpecOutputDescription(1),
        InvalidNumberOfItemSpecOutputResponse(0),
        InvalidNumberOfItemSpecOutputResponse(1),
        InvalidNumberOfItemSpecOutputResponse(2)
      )
  }

  @Test
  fun shouldInvalidWithOneItemSpecWithoutOneOutPutResponse() = runTest {
    SuiteSpec(simpleString.next()) {
        outputDescription { simpleString.next() }
        outputDescription { simpleString.next() }

        itemSpec(simpleString.next()) {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
        }

        itemSpec(simpleString.next()) {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
          outputResponse { simpleString.next() }
        }
      }
      .shouldBeLeft() shouldBe listOf<ValidationError>(InvalidNumberOfItemSpecOutputResponse(0))
  }

  @Test
  fun shouldInvalidWithTwoItemsSpecWithoutOneOutputResponse() = runTest {
    SuiteSpec(simpleString.next()) {
        outputDescription { simpleString.next() }
        outputDescription { simpleString.next() }

        itemSpec(simpleString.next()) {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
        }

        itemSpec(simpleString.next()) {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
        }
      }
      .shouldBeLeft() shouldBe
      listOf<ValidationError>(
        InvalidNumberOfItemSpecOutputResponse(0),
        InvalidNumberOfItemSpecOutputResponse(1),
      )
  }

  @Test
  fun shouldInvalidWithOneItemSpecWithEmptyOutputResponse() = runTest {
    val emptyOutputResponse = emptyString.next()

    SuiteSpec(simpleString.next()) {
        outputDescription { simpleString.next() }
        outputDescription { simpleString.next() }

        itemSpec(simpleString.next()) {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
          outputResponse { simpleString.next() }
        }

        itemSpec(simpleString.next()) {
          contextDescription { simpleString.next() }
          outputResponse { simpleString.next() }
          outputResponse { emptyOutputResponse }
        }
      }
      .shouldBeLeft() shouldBe listOf<ValidationError>(EmptyItemSpecOutputResponse(1))
  }
}
