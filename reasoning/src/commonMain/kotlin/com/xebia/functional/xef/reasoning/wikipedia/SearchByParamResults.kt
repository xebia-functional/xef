package com.xebia.functional.xef.reasoning.wikipedia

import kotlinx.serialization.Serializable

@Serializable data class SearchByParamResults(val pages: Map<String, Page>)
