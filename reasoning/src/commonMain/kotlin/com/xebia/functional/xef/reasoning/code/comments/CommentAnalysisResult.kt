package com.xebia.functional.xef.reasoning.code.comments

import kotlinx.serialization.Serializable

@Serializable data class CommentAnalysisResult(val analyses: List<CommentAnalysis>)
