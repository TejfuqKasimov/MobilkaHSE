package com.example.todolist

data class TaskItem(
    val id: Int = 0,                // Уникальный идентификатор
    val title: String,
    val isCompleted: Boolean = false
)