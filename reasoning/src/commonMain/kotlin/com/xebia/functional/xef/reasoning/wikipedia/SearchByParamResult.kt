package com.xebia.functional.xef.reasoning.wikipedia

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchByParamResult(@SerialName("query") val searchResults: SearchByParamResults)
