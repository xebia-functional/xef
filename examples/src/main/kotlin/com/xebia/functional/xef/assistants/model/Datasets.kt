package com.xebia.functional.xef.assistants.model

import kotlinx.serialization.Serializable

@Serializable
data class Datasets(
  val count: Int,
  val results: List<Dataset>
) {
  @Serializable
  data class Dataset(
    val name: String,
    val notes: String?,
    val files: List<DatasetFile>
  )

  @Serializable
  data class DatasetFile(
    val name: String,
    val url: String,
    val format: String,
    val mimetype: String,
    val size: String?,
    val created: String,
    val last_modified: String?
  )
  companion object {
    val empty: Datasets = Datasets(
      count = 0,
      results = emptyList()
    )
  }
}
