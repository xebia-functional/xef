package com.xebia.functional.xef.evaluator

import com.xebia.functional.xef.evaluator.models.ContextDescription
import com.xebia.functional.xef.evaluator.models.OutputDescription
import com.xebia.functional.xef.evaluator.models.OutputResponse
import com.xebia.functional.xef.evaluator.models.errors.EmptyContextDescription
import com.xebia.functional.xef.evaluator.models.errors.EmptyOutputDescription
import com.xebia.functional.xef.evaluator.models.errors.EmptyOutputResponse
import com.xebia.functional.xef.evaluator.utils.Generators.simpleString
import com.xebia.functional.xef.evaluator.utils.Generators.emptyString
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.property.arbitrary.next

class TestModelsSpec : ShouldSpec({

  context("build OutputDescription") {
    should("Valid") {
      OutputDescription { simpleString.next() }.shouldBeRight()
    }

    should("Invalid: when input is blank/empty") {
      OutputDescription { emptyString.next() } shouldBeLeft EmptyOutputDescription
    }
  }

  context("build OutputResponse") {
    should("Valid") {
      OutputResponse { simpleString.next() }.shouldBeRight()
    }

    should("Invalid: when input is blank/empty") {
      OutputResponse { emptyString.next() } shouldBeLeft EmptyOutputResponse
    }
  }

  context("build ContextDescription") {
    should("Valid") {
      ContextDescription {  simpleString.next() }.shouldBeRight()
    }

    should("Invalid: when input is blank/empty") {
      ContextDescription { emptyString.next() } shouldBeLeft  EmptyContextDescription
    }
  }
})

