package com.xebia.functional.openai.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateThreadRequestToolResourcesFileSearch(
  @SerialName(value = "vector_store_ids") val vectorStoreIds: List<String>
)
