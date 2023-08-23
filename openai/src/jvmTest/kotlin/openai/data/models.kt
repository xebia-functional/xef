package openai.data

import kotlinx.serialization.Serializable

@Serializable data class Question(val question: String)

@Serializable data class Answer(val bar: String)
