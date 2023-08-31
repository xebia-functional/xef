package com.xebia.functional.xef.gcp

data class GcpConfig(
  val token: String,
  val projectId: String,
  /** [GCP locations](https://cloud.google.com/vertex-ai/docs/general/locations) */
  val location: VertexAIRegion, // Supported us-central1 or europe-west4
)

enum class VertexAIRegion(val officialName: String) {
  US_CENTRAL1("us-central1"),
  EU_WEST4("europe-west4"),
}