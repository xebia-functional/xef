package com.xebia.functional.xef.reasoning.serpapi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResults(@SerialName("organic_results") val searchResults: List<SearchResult>)
