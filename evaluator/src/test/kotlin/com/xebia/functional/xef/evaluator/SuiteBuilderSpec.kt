package com.xebia.functional.xef.evaluator

import com.xebia.functional.xef.evaluator.models.SuiteSpec
import com.xebia.functional.xef.evaluator.models.errors.*
import com.xebia.functional.xef.evaluator.utils.Generators.emptyString
import com.xebia.functional.xef.evaluator.utils.Generators.simpleString
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.next

class SuiteBuilderSpec :
  ShouldSpec({
    should("build a valid SuiteSpec") {
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

    context("descriptionValidator") {
      should("Invalid: with empty description") {
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
    }

    context("outputsDescriptionValidator") {
      should("Invalid: without output description provided") {
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

      should("Invalid: with more outputs descriptions that items specs") {
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

      should("Invalid: with less outputs description than items spec") {
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

      should("Invalid: with empty output description at index 0 and 1") {
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
    }

    context("itemsValidator") {
      should("Invalid: One ItemSpec without one outputResponse") {
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

      should("Invalid: SuiteSpec with two ItemsSpec without one outputResponse") {
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

      should("Invalid: SuiteSpec with one itemSpec with an empty outputResponse") {
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
  })
