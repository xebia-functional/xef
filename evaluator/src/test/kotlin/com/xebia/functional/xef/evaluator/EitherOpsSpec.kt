package com.xebia.functional.xef.evaluator

import com.xebia.functional.xef.evaluator.EitherOps.toJson
import com.xebia.functional.xef.evaluator.EitherOps.toJsonFile
import com.xebia.functional.xef.evaluator.models.SuiteSpec
import com.xebia.functional.xef.evaluator.utils.Generators.emptyString
import com.xebia.functional.xef.evaluator.utils.Generators.simpleString
import io.kotest.assertions.json.shouldBeJsonObject
import io.kotest.assertions.json.shouldBeValidJson
import io.kotest.assertions.json.shouldContainJsonKey
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.common.runBlocking
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.next
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class EitherOpsSpec {
  private val validSuiteSpec = runBlocking {
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
  }

  @Test
  fun shouldSerializeToJsonCorrectly() {
    val validJsonSuiteSpec = validSuiteSpec.toJson()

    validJsonSuiteSpec.shouldBeValidJson()
    validJsonSuiteSpec.shouldBeJsonObject()
    validJsonSuiteSpec.shouldContainJsonKey("$.description")
    validJsonSuiteSpec.shouldContainJsonKey("$.metric")
    validJsonSuiteSpec.shouldContainJsonKey("$.outputs_description")
    validJsonSuiteSpec.shouldContainJsonKey("$.minimum_score")
  }

  @Test
  fun shouldSerializeToJsonTheAccumulatedErrors() = runTest {
    val invalidJsonSuiteSpec =
      SuiteSpec(emptyString.next()) {
          outputDescription { emptyString.next() }
          outputDescription { emptyString.next() }

          itemSpec(emptyString.next()) {
            contextDescription { emptyString.next() }
            outputResponse { emptyString.next() }
          }
        }
        .toJson()

    val expectedStringJson =
      """[
        "SuiteSpec description is empty, please provide a description",
        "The number of outputs description are greater than the number of items spec, \nplease provide the same number of outputs description and items spec",
        "Empty output response at the ItemSpec: 0, please provide an output response"
      ]"""
        .trimIndent()

    invalidJsonSuiteSpec.shouldBeValidJson()
    invalidJsonSuiteSpec.shouldEqualJson(expectedStringJson)
  }

  @Test
  fun shouldSaveToFileTheJsonSuiteSpec() {
    val tempDir = createTempDirectory()

    validSuiteSpec.toJsonFile(tempDir.absolutePathString(), "data.json")

    val tempJsonFile = tempDir.listDirectoryEntries().first().toFile()

    tempJsonFile.name shouldBe "data.json"
    tempJsonFile.exists() shouldBe true
    tempJsonFile.readText().shouldBeValidJson()
  }
}
