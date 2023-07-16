import kotlinx.serialization.Serializable

@Serializable
data class DuplicateCodeDetectionResult(
  val duplicateSnippets: List<String>,
  val similarityThreshold: Double,
  val suggestedRefactor: String
)
