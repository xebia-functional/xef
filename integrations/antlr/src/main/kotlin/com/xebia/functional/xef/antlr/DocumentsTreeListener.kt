package com.xebia.functional.xef.antlr

import okio.Path
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.misc.Interval
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTreeListener
import org.antlr.v4.runtime.tree.TerminalNode

internal class DocumentsTreeListener(
  val tempFolder: Path,
  val entryPoint: EntryPoint,
  val docBuilder: MutableList<SourceDocument>) : ParseTreeListener {
  override fun visitTerminal(node: TerminalNode) {
  }

  override fun visitErrorNode(node: ErrorNode) {
  }

  override fun enterEveryRule(ctx: ParserRuleContext) {
  }

  private fun ParserRuleContext.sourceTextForContext(keepQuotes: Boolean): String {
    return sourceTextForRange(start, stop, keepQuotes)
  }

  private fun sourceTextForRange(start: Token, stop: Token?, keepQuotes: Boolean): String {
    val cs = start.tokenSource?.inputStream
    val stopIndex = stop?.stopIndex ?: Int.MAX_VALUE
    var result = cs?.getText(Interval(start.startIndex, stopIndex)) ?: ""
    if (keepQuotes || result.length < 2)
      return result

    val quoteChar = result[0]
    if ((quoteChar == '"' || quoteChar == '`' || quoteChar == '\'') && quoteChar == result.last()) {
      if (quoteChar == '"' || quoteChar == '\'') {
        // Replace any double occurrence of the quote char by a single one.
        result = result.replace(quoteChar.toString() + quoteChar, quoteChar.toString())
      }
      return result.substring(1, result.length - 1)
    }
    return result
  }

  override fun exitEveryRule(ctx: ParserRuleContext) {
    val label = ctx.javaClass.simpleName.substringAfterLast("$").substringBeforeLast("Context")
    val source = if (label in entryPoint.contextDocuments) {
      ctx.sourceTextForContext(false)
    } else {
      null
    }
    if (source != null) {
      val sourceDoc = SourceDocument(
        tempFolder = tempFolder,
        label = label,
        startOffset = ctx.start.startIndex,
        endOffset = ctx.stop.stopIndex,
        source = source
      )
      docBuilder.add(sourceDoc)
    }
  }
}
