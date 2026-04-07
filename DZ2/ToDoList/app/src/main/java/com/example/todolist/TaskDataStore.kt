package com.example.todolist

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATA_STORE_NAME = "tasks_prefs"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

class TaskDataStore(private val context: Context) {

    private val gson = Gson()
    private val tasksKey = stringPreferencesKey("tasks_list")

    // Поток списка задач
    val tasksFlow: Flow<List<TaskItem>> = context.dataStore.data.map { preferences ->
        val json = preferences[tasksKey] ?: "[]"
        val type = object : TypeToken<List<TaskItem>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    }

    // Сохранить список задач
    suspend fun saveTasks(tasks: List<TaskItem>) {
        context.dataStore.edit { preferences ->
            val json = gson.toJson(tasks)
            preferences[tasksKey] = json
        }
    }
}