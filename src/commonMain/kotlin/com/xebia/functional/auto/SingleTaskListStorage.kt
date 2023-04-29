package com.xebia.functional.auto

class SingleTaskListStorage {
    private val tasks = ArrayDeque<Task>()
    private var taskIdCounter = 0

    fun append(task: Task) {
        tasks.add(task)
    }

    fun replace(tasks: List<Task>) {
        this.tasks.clear()
        this.tasks.addAll(tasks)
    }

    fun popleft(): Task {
        return tasks.removeFirst()
    }

    fun isEmpty(): Boolean {
        return tasks.isEmpty()
    }

    fun nextTaskId(): Int {
        taskIdCounter += 1
        return taskIdCounter
    }

    fun getTasksIds(): List<TaskId> {
        return tasks.map { it.id }
    }

    fun getTasks(): List<Task> {
        return tasks
    }
}
