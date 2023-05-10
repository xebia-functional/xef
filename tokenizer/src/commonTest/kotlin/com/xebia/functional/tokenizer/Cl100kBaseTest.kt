package com.xebia.functional.tokenizer

import com.goncalossilva.resources.Resource
import com.xebia.functional.tokenizer.EncodingType.CL100K_BASE
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class Cl100kBaseTest {
  private val resource = Resource("src/commonTest/resources/cl100k_base_encodings.csv")

  @Test
  fun cl100kBaseEncodesCorrectly() {
    resource.splitCSV().forEachIndexed { index, (input, output, _) ->
      val actual = CL100K_BASE.encoding.encode(input)
      val expected = output.parseEncoding()
      println(input)
      withClue("[${index + 1}]: $input") {
        actual shouldBe expected
      }
    }
  }
}

fun Resource.splitCSV() =
  readText().lineSequence()
    .drop(1)
    .map(String::parseCSVRow)

private fun String.parseCSVRow(): List<String> =
  buildList {
    val currentColumn = StringBuilder()
    var withinQuotes = false
    var skipNextChar = false

    for (i in this@parseCSVRow.indices) {
      if (skipNextChar) {
        skipNextChar = false
        continue
      }

      val currentChar = this@parseCSVRow[i]
      val nextChar = if (i < length - 1) this@parseCSVRow[i + 1] else null

      when {
        currentChar == '"' -> {
          if (withinQuotes && nextChar == '"') {
            currentColumn.append('"') // Handle escaped double quotes ""
            skipNextChar = true
          } else {
            withinQuotes = !withinQuotes
          }
        }

        currentChar == ',' && !withinQuotes -> {
          add(currentColumn.toString())
          currentColumn.clear()
        }

        else -> currentColumn.append(currentChar)
      }
    }
    add(currentColumn.toString())
  }

fun String.parseEncoding(): List<Int> =
  substring(1, length - 1)
    .replace(" ", "")
    .split(",")
    .dropLastWhile { it.isEmpty() }
    .map { it.toInt() }
