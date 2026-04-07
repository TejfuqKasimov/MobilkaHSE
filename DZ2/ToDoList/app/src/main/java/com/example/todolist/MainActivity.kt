package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.ui.theme.ToDoListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToDoListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskApp(taskViewModel: TaskViewModel = viewModel()) {
    val tasks by taskViewModel.tasks.collectAsState()
    val editingTaskId by taskViewModel.editingTaskId.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Заголовок
        TopAppBar(
            title = { Text("Список задач", color = MaterialTheme.colorScheme.onPrimary) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        )

        // Список задач
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskItemRow(
                    task = task,
                    isEditing = editingTaskId == task.id,
                    onToggleCompleted = { taskViewModel.toggleCompleted(task) },
                    onDelete = { taskViewModel.deleteTask(task) },
                    onStartEdit = { taskViewModel.startEditing(task.id) },
                    onSaveEdit = { newTitle -> taskViewModel.updateTaskTitle(task.id, newTitle) },
                    onCancelEdit = { taskViewModel.cancelEditing() }
                )
            }
        }

        // Панель добавления новой задачи
        AddTaskPanel(
            onAddTask = { title -> taskViewModel.addTask(title) }
        )
    }
}

@Composable
fun TaskItemRow(
    task: TaskItem,
    isEditing: Boolean,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    onStartEdit: () -> Unit,
    onSaveEdit: (String) -> Unit,
    onCancelEdit: () -> Unit
) {
    var editText by remember(task.id, isEditing) { mutableStateOf(task.title) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // При входе в режим редактирования запрашиваем фокус
    LaunchedEffect(isEditing) {
        if (isEditing) {
            focusRequester.requestFocus()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Чекбокс для отметки выполнения
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleCompleted() }
            )

            // Текст или поле ввода
            if (isEditing) {
                BasicTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onSaveEdit(editText)
                            focusManager.clearFocus()
                        }
                    ),
                    singleLine = true
                )
            } else {
                Text(
                    text = task.title,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    color = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface,
                    fontStyle = if (task.isCompleted) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
            }

            // Кнопки действий
            if (isEditing) {
                IconButton(onClick = { onSaveEdit(editText) }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Сохранить",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onCancelEdit) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Отмена",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                IconButton(onClick = onStartEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AddTaskPanel(onAddTask: (String) -> Unit) {
    var newTaskText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTaskText,
                onValueChange = { newTaskText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Новая задача") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (newTaskText.isNotBlank()) {
                            onAddTask(newTaskText)
                            newTaskText = ""
                            focusManager.clearFocus()
                        }
                    }
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newTaskText.isNotBlank()) {
                        onAddTask(newTaskText)
                        newTaskText = ""
                        focusManager.clearFocus()
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
                Text("Добавить")
            }
        }
    }
}