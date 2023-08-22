package com.xebia.functional.xef.reasoning.wikipedia

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class SearchResult(@SerialName("query") val searchResults: SearchResults)
