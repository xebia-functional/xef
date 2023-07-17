import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import com.xebia.functional.xef.reasoning.tools.ToolOutput
import kotlinx.serialization.Serializable

@Serializable
data class CodeBreakdownResult(val breakdownCode: String) : Tool.Out<CodeBreakdownResult> {
  override fun toolOutput(metadata: ToolMetadata): ToolOutput<CodeBreakdownResult> {
    return ToolOutput(metadata, listOf(breakdownCode), this)
  }
}
