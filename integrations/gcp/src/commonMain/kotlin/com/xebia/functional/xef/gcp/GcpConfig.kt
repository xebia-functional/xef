package com.xebia.functional.xef.gcp

data class GcpConfig(
  val token: String,
  val projectId: String,
  /** https://cloud.google.com/vertex-ai/docs/general/locations */
  val location: String, // Supported us-central1 or europe-west4
)
