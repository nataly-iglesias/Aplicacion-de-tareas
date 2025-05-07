package com.equipouno.aplicaciondetareas.tasks

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val deadline: String,
    val priority: String,
    val category: String,
    var recurrence: String? = null,
    var isCompleted: Boolean
)
