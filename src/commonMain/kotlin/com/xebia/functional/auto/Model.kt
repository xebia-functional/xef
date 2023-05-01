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
data class Task(val id: TaskId, val objective: Objective)

@Serializable
data class TaskWithResult(val task: Task, val result: TaskResult) {
  fun toJson(): String = Json.encodeToString(this)
  fun value(): String = result.value()
  fun isCompleted(): Boolean = result.isCompleted()
  fun isFailed(): Boolean = result.isFailed()

  companion object {
    fun fromJson(json: String): TaskWithResult =
      Json.decodeFromString(json)
  }
}

@JvmInline
@Serializable
value class TaskResult(private val value: String) {
  fun value(): String =
    value.replace(COMPLETED, "").replace(FAILED, "").trim()

  fun isCompleted(): Boolean =
    value.endsWith(COMPLETED)

  fun isFailed(): Boolean =
    value.endsWith(FAILED)
}
