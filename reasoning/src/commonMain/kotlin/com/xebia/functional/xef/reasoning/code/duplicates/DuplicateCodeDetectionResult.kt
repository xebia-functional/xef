import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import com.xebia.functional.xef.reasoning.tools.ToolOutput
import kotlinx.serialization.Serializable

@Serializable
data class DuplicateCodeDetectionResult(
  val duplicateSnippets: List<String>,
  val similarityThreshold: Double,
  val suggestedRefactor: String
) : Tool.Out<DuplicateCodeDetectionResult> {
  override fun toolOutput(metadata: ToolMetadata): ToolOutput<DuplicateCodeDetectionResult> {
    return ToolOutput(
      metadata,
      listOf(
        """|
      |Duplicate code snippets:
      |${duplicateSnippets.joinToString("\n")}
      |
      |Similarity threshold: $similarityThreshold
      |
      |Suggested refactor:
      |${suggestedRefactor}
    """
          .trimMargin()
      ),
      this
    )
  }
}
