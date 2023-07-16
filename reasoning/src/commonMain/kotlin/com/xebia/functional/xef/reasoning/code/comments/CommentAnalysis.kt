package com.xebia.functional.xef.reasoning.code.comments

import kotlinx.serialization.Serializable

@Serializable
data class CommentAnalysis(
  val comment: String,
  val quality: Quality,
  val completeness: Completeness,
  val usefulness: Usefulness
)
