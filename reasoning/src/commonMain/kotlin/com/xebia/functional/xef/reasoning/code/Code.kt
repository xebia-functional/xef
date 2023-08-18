package com.xebia.functional.xef.reasoning.code

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.reasoning.tools.LLMTool
import com.xebia.functional.xef.reasoning.tools.Tool
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

class Code
@JvmOverloads
constructor(
  model: Chat,
  serialization: ChatWithFunctions,
  scope: Conversation,
  @JvmField
  val diffSummaryFromUrl: DiffSummary =
    DiffSummary(serialization = serialization, chat = model, scope = scope),
  @JvmField
  val antiPatternDetection: LLMTool =
    LLMTool.create(
      name = "AntiPatternDetection",
      description = "Detect anti-patterns in code",
      model = model,
      scope = scope
    ),
  @JvmField
  val apiUsageExampleGeneration: LLMTool =
    LLMTool.create(
      name = "ApiUsageExampleGeneration",
      description = "Generate API usage examples",
      model = model,
      scope = scope
    ),
  @JvmField
  val bugDetection: LLMTool =
    LLMTool.create(
      name = "BugDetection",
      description = "Detect bugs in code",
      model = model,
      scope = scope
    ),
  @JvmField
  val commentAnalyzer: LLMTool =
    LLMTool.create(
      name = "CommentAnalyzer",
      description = "Analyze comments in code",
      model = model,
      scope = scope
    ),
  @JvmField
  val coreDocumentationGeneration: LLMTool =
    LLMTool.create(
      name = "CoreDocumentationGeneration",
      description = "Generate core documentation",
      model = model,
      scope = scope
    ),
  @JvmField
  val duplicateCodeDetection: LLMTool =
    LLMTool.create(
      name = "DuplicateCodeDetection",
      description = "Detect duplicate code",
      model = model,
      scope = scope
    ),
  @JvmField
  val performanceOptimization: LLMTool =
    LLMTool.create(
      name = "PerformanceOptimization",
      description = "Optimize performance",
      model = model,
      scope = scope
    ),
  @JvmField
  val codeRefactoring: LLMTool =
    LLMTool.create(
      name = "CodeRefactoring",
      description = "Refactor code",
      model = model,
      scope = scope
    ),
  @JvmField
  val codeBreakdown: LLMTool =
    LLMTool.create(
      name = "CodeBreakdown",
      description = "Breakdown code",
      model = model,
      scope = scope
    ),
  @JvmField
  val testGeneration: LLMTool =
    LLMTool.create(
      name = "TestGeneration",
      description = "Generate tests",
      model = model,
      scope = scope
    ),
  @JvmField
  val vulnerabilityScanning: LLMTool =
    LLMTool.create(
      name = "VulnerabilityScanning",
      description = "Scan for vulnerabilities",
      model = model,
      scope = scope
    )
) {

  val tools: List<Tool> =
    listOf(
      antiPatternDetection,
      apiUsageExampleGeneration,
      bugDetection,
      commentAnalyzer,
      coreDocumentationGeneration,
      duplicateCodeDetection,
      performanceOptimization,
      codeRefactoring,
      codeBreakdown,
      testGeneration,
      vulnerabilityScanning
    )

  companion object {

    @JvmStatic
    @JvmName("create")
    operator fun invoke(model: Chat, scope: Conversation): Code = Code(model, scope)
  }
}
