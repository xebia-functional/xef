package com.xebia.functional.xef.csv

/**
 * Constructor used by the CSVParserBuilder.
 *
 * @param quotechar The character to use for quoted elements
 * @param separator The delimiter to use for separating entries
 *
 * A port of
 * [OpenCSV](https://github.com/cygri/opencsv/blob/master/src/main/java/com/opencsv/RFC4180Parser.java)
 */
class RFC4180Parser(val quotechar: Char = '"', val separator: Char = ',') {

  /**
   * Parses an incoming String and returns an array of elements. This method is used when all data
   * is contained in a single line.
   *
   * @param nextLine Line to be parsed.
   * @return The list of elements
   */
  fun parseLine(nextLine: String): List<String> =
    if (!nextLine.contains(quotechar)) nextLine.split(separator)
    else splitWhileNotInQuotes(nextLine).map(::handleQuotes)

  private fun splitWhileNotInQuotes(nextLine: String): List<String> = buildList {
    var currentPosition = 0
    var nextSeparator: Int
    var nextQuote: Int
    while (currentPosition < nextLine.length) {
      nextSeparator = nextLine.indexOf(separator, currentPosition)
      nextQuote = nextLine.indexOf(quotechar, currentPosition)

      currentPosition =
        when {
          nextSeparator == -1 -> {
            add(nextLine.substring(currentPosition))
            nextLine.length
          }
          nextQuote == -1 || nextQuote > nextSeparator || nextQuote != currentPosition -> {
            add(nextLine.substring(currentPosition, nextSeparator))
            nextSeparator + 1
          }
          else -> {
            val fieldEnd = findEndOfFieldFromPosition(nextLine, currentPosition)
            add(
              if (fieldEnd >= nextLine.length) nextLine.substring(currentPosition)
              else nextLine.substring(currentPosition, fieldEnd)
            )
            fieldEnd + 1
          }
        }
    }
  }

  private fun findEndOfFieldFromPosition(nextLine: String, currentPosition: Int): Int {
    var nextQuote = nextLine.indexOf(quotechar, currentPosition + 1)
    while (nextQuote != -1) {
      if (nextLine.getOrNull(nextQuote + 1) == separator) {
        return nextQuote + 1
      }
      nextQuote = nextLine.indexOf(quotechar, nextQuote + 1)
    }
    return nextLine.length
  }

  private fun handleQuotes(element: String): String =
    element
      .removeSurrounding(quotechar.toString())
      .replace(quotechar.toString() + quotechar.toString(), quotechar.toString())
}
