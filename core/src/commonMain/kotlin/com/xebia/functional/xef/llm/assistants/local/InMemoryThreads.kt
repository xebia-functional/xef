package com.xebia.functional.xef.llm.assistants.local

import arrow.fx.coroutines.Atomic
import com.xebia.functional.openai.generated.model.CreateThreadRequest
import com.xebia.functional.openai.generated.model.ModifyThreadRequest
import com.xebia.functional.openai.generated.model.ThreadObject
import com.xebia.functional.xef.server.assistants.utils.AssistantUtils
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class InMemoryThreads : AssistantPersistence.Thread {

  private val threads = Atomic.unsafe(emptyMap<UUID, ThreadObject>())

  override suspend fun get(threadId: String): ThreadObject {
    return threads.get()[UUID(threadId)] ?: throw Exception("Thread not found for id: $threadId")
  }

  override suspend fun delete(threadId: String): Boolean {
    val uuid = UUID(threadId)
    return !threads.updateAndGet { it.filter { (id, _) -> id != uuid } }.containsKey(uuid)
  }

  override suspend fun create(
    assistantId: String?,
    runId: String?,
    createThreadRequest: CreateThreadRequest
  ): ThreadObject {
    val uuid = UUID.generateUUID()
    val threadObject = AssistantUtils.threadObject(uuid, createThreadRequest)
    val threadId = UUID(threadObject.id)
    threads.update { it + (threadId to threadObject) }
    return threadObject
  }

  override suspend fun modify(
    threadId: String,
    modifyThreadRequest: ModifyThreadRequest
  ): ThreadObject {
    val thread = get(threadId)
    val uuid = UUID(threadId)
    val modifiedThread = thread.copy(metadata = modifyThreadRequest.metadata ?: thread.metadata)
    threads.update { it + (uuid to modifiedThread) }
    return modifiedThread
  }
}
