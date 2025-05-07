package com.equipouno.aplicaciondetareas.tasks

object TaskRepository {
    val pendingTasks = mutableListOf<Task>()
    val completedTasks = mutableListOf<Task>()

    fun addTask(task: Task) {
        pendingTasks.add(task)
    }

    fun markTaskAsCompleted(task: Task) {
        pendingTasks.remove(task)
        task.isCompleted = true
        completedTasks.add(task)
    }
}
