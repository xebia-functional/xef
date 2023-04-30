package com.xebia.functional.auto

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class Objective(val value: String)

@JvmInline
value class LLM(val value: String)

@JvmInline
@Serializable
value class TaskId(val id: Int)

@JvmInline
value class User(val name: String)

@Serializable
data class Task(
    val id: TaskId,
    val objective: Objective,
    val resultId: TaskId? = null,
    val result: String? = null,
) {
    fun toJson(): String =
        Json.encodeToString(this)

    companion object {
        fun fromJson(json: String): Task =
            Json.decodeFromString(json)
    }
}

@JvmInline
value class TaskResult(val value: String)
