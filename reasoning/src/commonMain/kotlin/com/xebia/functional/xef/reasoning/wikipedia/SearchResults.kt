package com.xebia.functional.xef.reasoning.wikipedia

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class SearchResults(@SerialName("search") val searches: List<SearchData>)
