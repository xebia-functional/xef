package com.xebia.functional.xef.llm.assistants

enum class RunDeltaEvent {
  ThreadCreated,
  ThreadRunCreated,
  ThreadRunQueued,
  ThreadRunInProgress,
  ThreadRunRequiresAction,
  ThreadRunCompleted,
  ThreadRunFailed,
  ThreadRunCancelling,
  ThreadRunCancelled,
  ThreadRunExpired,
  ThreadRunStepCreated,
  ThreadRunStepInProgress,
  ThreadRunStepDelta,
  ThreadRunStepCompleted,
  ThreadRunStepFailed,
  ThreadRunStepCancelled,
  ThreadRunStepExpired,
  ThreadMessageCreated,
  ThreadMessageInProgress,
  ThreadMessageDelta,
  ThreadMessageCompleted,
  ThreadMessageIncomplete,
  Error
}
