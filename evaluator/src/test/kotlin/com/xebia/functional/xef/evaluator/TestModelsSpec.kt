package com.xebia.functional.xef.evaluator

import com.xebia.functional.xef.evaluator.models.ContextDescription
import com.xebia.functional.xef.evaluator.models.OutputDescription
import com.xebia.functional.xef.evaluator.models.OutputResponse
import com.xebia.functional.xef.evaluator.models.errors.EmptyContextDescription
import com.xebia.functional.xef.evaluator.models.errors.EmptyOutputDescription
import com.xebia.functional.xef.evaluator.models.errors.EmptyOutputResponse
import com.xebia.functional.xef.evaluator.utils.Generators.emptyString
import com.xebia.functional.xef.evaluator.utils.Generators.simpleString
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.property.arbitrary.next
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TestModelsSpec {

  @Test
  fun shouldBuildOutputDescription() = runTest {
    OutputDescription { simpleString.next() }.shouldBeRight()
  }

  @Test
  fun shouldBeInvalidWhenOutputDescriptionIsEmpty() = runTest {
    OutputDescription { emptyString.next() } shouldBeLeft EmptyOutputDescription
  }

  @Test
  fun shouldBuildOutputResponse() = runTest {
    OutputResponse { simpleString.next() }.shouldBeRight()
  }

  @Test
  fun shouldBeInvalidWhenOutputResponseIsEmpty() = runTest {
    OutputResponse { emptyString.next() } shouldBeLeft EmptyOutputResponse
  }

  @Test
  fun shouldBuildContextDescription() = runTest {
    ContextDescription { simpleString.next() }.shouldBeRight()
  }

  @Test
  fun shouldBeInvalidWhenItemASContextDescriptionIsEmpty() = runTest {
    ContextDescription { emptyString.next() } shouldBeLeft EmptyContextDescription
  }
}
