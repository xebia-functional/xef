package com.xebia.functional.xef.auto.reasoning

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.OpenAIEmbeddings
import com.xebia.functional.xef.reasoning.code.Code

suspend fun main() {
  val scope = CoreAIScope(OpenAIEmbeddings(OpenAI.DEFAULT_EMBEDDING))
  val code = Code(chatModel = OpenAI.DEFAULT_CHAT, serializationModel = OpenAI.DEFAULT_SERIALIZATION, scope = scope)

  val sourceCode = """
       import java.util.*

       class ShoppingCart {
           private val items: MutableList<String> = mutableListOf()

           fun addItem(item: String) {
               items.add(item)
           }

           fun removeItem(item: String) {
               items.remove(item)
           }

           fun getTotalItems(): Int {
               return items.size
           }

           fun calculateTotalPrice(): Double {
               var totalPrice = 0.0
               for (item in items) {
                   val price = fetchItemPrice(item)
                   totalPrice += price
               }
               return totalPrice
           }

           private fun fetchItemPrice(item: String): Double {
               // Logic to fetch item price from database or API
               // For simplicity, return a random price
               return Random().nextDouble() * 100
           }
       }

       fun main() {
           val cart = ShoppingCart()
           cart.addItem("Item 1")
           cart.addItem("Item 2")
           cart.addItem("Item 3")

           println("Total items in cart: ${'$'}{cart.getTotalItems()}")
           println("Total price of items in cart: ${'$'}{cart.calculateTotalPrice()}")

           cart.removeItem("Item 2")

           println("Total items in cart: ${'$'}{cart.getTotalItems()}")
           println("Total price of items in cart: ${'$'}{cart.calculateTotalPrice()}")
       }

    """.trimIndent()

  val antiPatternDetectionResult = code.antiPatternDetection.detectAntiPatterns(sourceCode)
  println("Detected Anti-Patterns:")
  antiPatternDetectionResult.detectedAntiPatterns.forEach { println(it) }
  println()

  val apiUsageExamplesResult = code.apiUsageExampleGeneration.generateUsageExamples(listOf("API1", "API2", "API3"))
  println("Generated API Usage Examples:")
  apiUsageExamplesResult.examples.forEach { println(it) }
  println()

  val bugDetectionResult = code.bugDetection.detectBugs(sourceCode)
  println("Detected Bugs:")
  bugDetectionResult.bugs.forEach { println(it) }
  println()

  val commentAnalysisResult = code.commentAnalyzer.analyzeComments(sourceCode)
  println("Code Comment Analysis:")
  commentAnalysisResult.analyses.forEach { println(it) }
  println()

  val codeDocumentationResult = code.coreDocumentationGeneration.generateCodeDocumentation(sourceCode)
  println("Generated Code Documentation:")
  println(codeDocumentationResult)
  println()

  val codeRefactoringResult = code.codeRefactoring.refactorCode(sourceCode)
  println("Code Refactoring Suggestions:")
  codeRefactoringResult.refactoredCode.forEach { println(it) }
  println()

  val performanceOptimizationResult = code.performanceOptimization.optimizePerformance(sourceCode)
  println("Performance Optimization Suggestions:")
  performanceOptimizationResult.recommendations.forEach { println(it) }
  println()

  val testGenerationResult = code.testGeneration.generateTestCases(sourceCode)
  println("Generated Test Cases:")
  testGenerationResult.testCases.forEach { println(it) }
  println()

  val vulnerabilityScanningResult = code.vulnerabilityScanning.scanForVulnerabilities(sourceCode)
  println("Vulnerability Scanning Results:")
  vulnerabilityScanningResult.detectedVulnerabilities.forEach { println(it) }
  println()
}
