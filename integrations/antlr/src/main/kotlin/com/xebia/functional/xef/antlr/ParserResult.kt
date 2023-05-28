package com.xebia.functional.xef.antlr

import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.ParserRuleContext

data class ParserResult(val parser: Parser, val context: ParserRuleContext, val docs: List<SourceDocument>)
