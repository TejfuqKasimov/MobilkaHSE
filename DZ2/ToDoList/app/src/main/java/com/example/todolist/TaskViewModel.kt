package com.example.todolist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = TaskDataStore(application)

    // Текущий список задач
    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    // ID задачи, которая сейчас редактируется (-1 если нет)
    private val _editingTaskId = MutableStateFlow(-1)
    val editingTaskId: StateFlow<Int> = _editingTaskId.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.tasksFlow.collect { savedTasks ->
                _tasks.value = savedTasks
            }
        }
    }

    fun addTask(title: String) {
        if (title.isNotBlank()) {
            val newId = (_tasks.value.maxOfOrNull { it.id } ?: 0) + 1
            val newTask = TaskItem(id = newId, title = title)
            updateTasks(_tasks.value + newTask)
        }
    }

    fun deleteTask(task: TaskItem) {
        updateTasks(_tasks.value.filter { it.id != task.id })
        if (_editingTaskId.value == task.id) {
            _editingTaskId.value = -1
        }
    }

    fun toggleCompleted(task: TaskItem) {
        val updated = _tasks.value.map {
            if (it.id == task.id) it.copy(isCompleted = !it.isCompleted) else it
        }
        updateTasks(updated)
    }

    fun updateTaskTitle(taskId: Int, newTitle: String) {
        if (newTitle.isNotBlank()) {
            val updated = _tasks.value.map {
                if (it.id == taskId) it.copy(title = newTitle) else it
            }
            updateTasks(updated)
        }
        _editingTaskId.value = -1
    }

    fun startEditing(taskId: Int) {
        _editingTaskId.value = taskId
    }

    fun cancelEditing() {
        _editingTaskId.value = -1
    }

    private fun updateTasks(newList: List<TaskItem>) {
        _tasks.value = newList
        viewModelScope.launch {
            dataStore.saveTasks(newList)
        }
    }
}