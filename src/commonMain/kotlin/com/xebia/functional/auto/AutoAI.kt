package com.xebia.functional.auto

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.xebia.functional.llm.openai.ChatCompletionRequest
import com.xebia.functional.llm.openai.ChatCompletionResponse
import com.xebia.functional.llm.openai.Message
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.llm.openai.Role
import com.xebia.functional.vectorstores.VectorStore

class AutoAI(
  private val model: LLM,
  private val user: User,
  private val openAIClient: OpenAIClient,
  private val objective: Objective,
  private val vectorStore: VectorStore,
  private val resultsStorage: DefaultResultsStorage = DefaultResultsStorage(vectorStore),
  private val tasksStorage: SingleTaskListStorage = SingleTaskListStorage()
) {

  init {
    setupInitialTask()
  }

  private fun setupInitialTask() {
    val initialTask = Task(
      id = TaskId(tasksStorage.nextTaskId()),
      objective = objective
    )
    tasksStorage.append(initialTask)
  }

  private object TaskCompleted

  private suspend fun Raise<TaskCompleted>.taskCreationAgent(
    objective: Objective,
    result: String,
    taskDescription: String,
    taskList: List<TaskId>
  ): List<String> {
    val ids = taskList.joinToString(", ") { it.id.toString() }
    val prompt = """
            You are a task creation AI that uses the result of an execution agent to create new tasks with the following objective: ${objective.value},
            The last completed task has the result: $result.
            This result was based on this task description: $taskDescription. These are incomplete tasks: $ids.
            Based on the result, create new tasks to be completed by the AI system that do not overlap with incomplete tasks.
            Return the tasks as an array.
            IMPORTANT!!! : If there are no new tasks to complete and you determine the original objective:[${objective.value}] has been accomplished simply return:$COMPLETED""".trimIndent()
    val response = chatCompletionResponse(prompt)
    val resultMessage = getFirstMessage(response)
    ensure(!taskHasCompleted(resultMessage)) { TaskCompleted }
    return messageToTaskAsStrings(resultMessage)
  }

  private fun messageToTaskAsStrings(firstMessage: String?): List<String> =
    firstMessage?.split("\n") ?: listOf(firstMessage ?: "")

  private suspend fun prioritizationAgent() {
    val tasks = tasksStorage.getTasks().joinToString("\n") { "${it.id.id}. ${it.objective.value}" }
    val nextTaskId = tasksStorage.nextTaskId()
    val prompt = """
            |You are a task prioritization AI tasked with cleaning the formatting of and re-prioritizing the following tasks:
            |$tasks
            |Consider the ultimate objective of your team: ${objective.value}.
            |Do not remove any tasks. Return the result as a numbered list, like:
            |#. First task
            |#. Second task
            |Start the task list with number $nextTaskId.""".trimMargin()
    val response = chatCompletionResponse(prompt)
    val firstMessage = getFirstMessage(response)
    val newTasks = messageToTaskAsStrings(firstMessage)
    val newTasksList = mutableListOf<Task>()

    for (taskString in newTasks) {
      val taskParts = taskString.trim().split(".", limit = 2)
      if (taskParts.size == 2) {
        val taskId = taskParts[0].trim()
        val taskName = taskParts[1].trim()
        newTasksList.add(Task(TaskId(taskId.toInt()), Objective(taskName)))
      }
    }
    tasksStorage.replace(newTasksList)
  }

  private fun List<Task>.print(): String =
    joinToString("; ") { "${it.id.id}. ${it.objective.value} -> result: ${it.result}" }

  /**
   * The execution agent is the AI that performs the task
   */
  private suspend fun executionAgent(objective: Objective, task: Task): ChatCompletionResponse {
    val context = contextAgent(query = objective, topResultsNum = 5)
    val prompt = """
            |You are an AI who performs one task based on the following objective: 
            |${objective.value}
            |Take into account these previously completed tasks:
            |${context.print()}.
            |If you think your response is correct given the objective, 
            |return the Response ending with the delimiter: $COMPLETED
            |
            |Your task:
            |${task.objective.value}
            |Response:""".trimMargin()
    return chatCompletionResponse(prompt)
  }

  /**
   * Call the remote Open AI API to complete the task
   */
  private suspend fun chatCompletionResponse(prompt: String): ChatCompletionResponse {
    val completionRequest = ChatCompletionRequest(
      model = model.value, listOf(
        Message(Role.system.name, prompt, user.name)
      ), user = user.name
    )
    return openAIClient.createChatCompletion(completionRequest)
  }

  /**
   * Print the current tasks
   */
  private fun printCurrentTasks() {
    val allTasks = tasksStorage.getTasks().joinToString(separator = "\n") { "${it.id.id}. ${it.objective.value}" }
    println(allTasks)
  }

  /**
   * Get tasks from the result storage providing context
   * to the execution agent
   */
  private suspend fun contextAgent(query: Objective, topResultsNum: Int): List<Task> =
    resultsStorage.query(query, topResultsNum)

  /**
   * Execute the task through the AI and return the result
   */
  suspend operator fun invoke(): List<Task> {
    while (true) {
      // As long as there are tasks in the storage...
      if (!tasksStorage.isEmpty()) {
        // Print the task list
        printCurrentTasks()
        // Pull the first incomplete task
        val task = tasksStorage.popleft()
        // Send to execution function to complete the task based on the context
        val (resultMessage, taskWithResult) = executeAndStoreTask(task)
        // If the task has been completed, return the results
        if (taskHasCompleted(resultMessage)) {
          return resultsStorage.query(objective, Int.MAX_VALUE)
        }
        // Otherwise, send the result to the task creation agent to create new tasks
        when (val newTasks = getNewTasksOrComplete(resultMessage, taskWithResult)) {
          // If the task creation agent determines the objective has been completed, return the results
          is Either.Left -> {
            return resultsStorage.query(objective, Int.MAX_VALUE)
          }
          // Otherwise, add the new tasks to the storage and send to the prioritization agent
          is Either.Right -> {
            addTasksToStorage(newTasks.value)
            prioritizationAgent()
          }
        }
      }
    }
  }

  /**
   * Add new tasks to the storage
   */
  private fun addTasksToStorage(newTasks: List<String>) {
    for (newTask in newTasks) {
      val new = Task(
        id = TaskId(tasksStorage.nextTaskId()),
        objective = Objective(newTask)
      )
      tasksStorage.append(new)
    }
  }

  /**
   * If the task has been completed, return the results. Otherwise, send the result to the task creation agent to create new tasks
   */
  private suspend fun getNewTasksOrComplete(
    firstMessage: String,
    taskWithResult: Task
  ): Either<TaskCompleted, List<String>> = either {
    taskCreationAgent(
      objective = objective,
      result = firstMessage,
      taskDescription = taskWithResult.objective.value,
      taskList = tasksStorage.getTasksIds(),
    )
  }

  /**
   * Check if the task has been completed
   */
  private fun taskHasCompleted(result: String?): Boolean =
    result?.endsWith(COMPLETED) == true

  /**
   * Execute the task and store the result
   */
  private suspend fun executeAndStoreTask(task: Task): Pair<String, Task> {
    val result = executionAgent(objective = objective, task = task)
    val firstMessage = requireNotNull(getFirstMessage(result)) { "No message returned" }
    val cleanedMessage = cleanResultMessage(firstMessage)
    val taskWithResult = task.copy(result = cleanedMessage, resultId = TaskId(task.id.id))
    resultsStorage.add(taskWithResult)
    return Pair(firstMessage, taskWithResult)
  }

  private fun getFirstMessage(response: ChatCompletionResponse): String? =
    response.choices.firstOrNull()?.message?.content

  /** Clean the result message */
  private fun cleanResultMessage(firstMessage: String): String = firstMessage
    .replace(COMPLETED, "")
    .replace(FAILED, "")
    .trim()

  private val COMPLETED = "%COMPLETED%"
  private val FAILED = "%FAILED%"
}

