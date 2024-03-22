package com.xebia.functional.tokenizer

import com.goncalossilva.resources.Resource

fun Resource.splitCSV() =
  readText().lineSequence()
    .drop(1)
    .map(String::parseCSVRow)

private fun String.parseCSVRow(): Triple<String, String, String> =
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
  }.let { row ->
    require(row.size == 3) { "Each row should contain 3 cells. $row" }
    val (input, output, outputWithMaxTokens) = row
    Triple(input, output, outputWithMaxTokens)
  }

fun String.parseEncoding(): List<Int> =
  substring(1, length - 1)
    .replace(" ", "")
    .split(",")
    .dropLastWhile { it.isEmpty() }
    .map { it.toInt() }
