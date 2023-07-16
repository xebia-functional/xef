package com.xebia.functional.xef.reasoning.code

import CodeBreakdown
import DuplicateCodeDetection
import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.reasoning.code.antipatterns.AntiPatternDetection
import com.xebia.functional.xef.reasoning.code.api.usage.example.APIUsageExampleGeneration
import com.xebia.functional.xef.reasoning.code.bug.BugDetection
import com.xebia.functional.xef.reasoning.code.comments.CommentAnalyzer
import com.xebia.functional.xef.reasoning.code.documentation.CodeDocumentationGeneration
import com.xebia.functional.xef.reasoning.code.performance.PerformanceOptimization
import com.xebia.functional.xef.reasoning.code.refactor.CodeRefactoring
import com.xebia.functional.xef.reasoning.code.tests.TestGeneration
import com.xebia.functional.xef.reasoning.code.vulnerabilities.VulnerabilityScanning
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

class Code(
  serializationModel: ChatWithFunctions,
  scope: CoreAIScope,
  @JvmField
  val antiPatternDetection: AntiPatternDetection = AntiPatternDetection(serializationModel, scope),
  @JvmField
  val apiUsageExampleGeneration: APIUsageExampleGeneration = APIUsageExampleGeneration(serializationModel, scope),
  @JvmField
  val bugDetection: BugDetection = BugDetection(serializationModel, scope),
  @JvmField
  val commentAnalyzer: CommentAnalyzer = CommentAnalyzer(serializationModel, scope),
  @JvmField
  val coreDocumentationGeneration: CodeDocumentationGeneration = CodeDocumentationGeneration(serializationModel, scope),
  @JvmField
  val duplicateCodeDetection: DuplicateCodeDetection = DuplicateCodeDetection(serializationModel, scope),
  @JvmField
  val performanceOptimization: PerformanceOptimization = PerformanceOptimization(serializationModel, scope),
  @JvmField
  val codeRefactoring: CodeRefactoring = CodeRefactoring(serializationModel, scope),
  @JvmField
  val codeBreakdown: CodeBreakdown = CodeBreakdown(serializationModel, scope),
  @JvmField
  val testGeneration: TestGeneration = TestGeneration(serializationModel, scope),
  @JvmField
  val vulnerabilityScanning: VulnerabilityScanning = VulnerabilityScanning(serializationModel, scope)
) {
  companion object {

    @JvmStatic
    @JvmName("create")
    operator fun invoke(serializationModel: ChatWithFunctions, scope: CoreAIScope): Code =
      Code(serializationModel, scope)

  }
}
