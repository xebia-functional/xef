import com.xebia.functional.xef.gcp.GcpClient

suspend fun main() {
  val apiEndpoint = "us-central1-aiplatform.googleapis.com"
  val projectId = "xefdemo"
  val modelId = "codechat-bison@001"
  val token = "TOKEN"
  GcpClient(apiEndpoint, projectId, modelId, token).use {
    it
      .promptMessage(
        "How can I reverse a list in python?",
      )
      .also(::println)
  }
}
