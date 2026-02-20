package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Модель города с названием и идентификатором для Gismeteo
data class City(val name: String, val id: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherApp(viewModel: WeatherViewModel = viewModel()) {
    val selectedCity by viewModel.selectedCity.collectAsState()
    val period by viewModel.period.collectAsState()
    val context = LocalContext.current

    // Состояние для раскрывающегося меню выбора города
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Выпадающий список для выбора города
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCity.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Город") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                viewModel.cities.forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city.name) },
                        onClick = {
                            viewModel.updateCity(city)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Выбор периода (для красоты, на Gismeteo не влияет)
        Text("Период прогноза:", style = MaterialTheme.typography.titleMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PeriodOption(
                text = "Сегодня",
                selected = period == Period.TODAY,
                onClick = { viewModel.updatePeriod(Period.TODAY) }
            )
            PeriodOption(
                text = "3 дня",
                selected = period == Period.THREE_DAYS,
                onClick = { viewModel.updatePeriod(Period.THREE_DAYS) }
            )
            PeriodOption(
                text = "10 дней",
                selected = period == Period.TEN_DAYS,
                onClick = { viewModel.updatePeriod(Period.TEN_DAYS) }
            )
            PeriodOption(
                text = "Выходные",
                selected = period == Period.WEEKEND,
                onClick = { viewModel.updatePeriod(Period.WEEKEND) }
            )
        }

        // Кнопка открытия в браузере
        Button(
            onClick = {
                val url = viewModel.buildUrl()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Открыть в браузере")
        }
    }
}

@Composable
fun PeriodOption(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) }
    )
}

enum class Period(val path: String) {
    TODAY(""),
    TEN_DAYS("10-days"),
    WEEKEND("weekend"),
    THREE_DAYS("3-days")
}

class WeatherViewModel : ViewModel() {
    // Список доступных городов
    val cities = listOf(
        City("Москва", "moscow-4368"),
        City("Санкт-Петербург", "sankt-peterburg-4079"),
        City("Нижний Новгород", "nizhny-novgorod-4355"),
        City("Дербент", "derbent-5268"),
        City("Казань", "kazan-4364"),
        City("Барнаул", "barnaul-4720")
    )

    private val _selectedCity = MutableStateFlow(cities.first()) // Москва по умолчанию
    val selectedCity: StateFlow<City> = _selectedCity.asStateFlow()

    private val _period = MutableStateFlow(Period.TODAY)
    val period: StateFlow<Period> = _period.asStateFlow()

    fun updateCity(city: City) {
        _selectedCity.value = city
    }

    fun updatePeriod(newPeriod: Period) {
        _period.value = newPeriod
    }

    fun buildUrl(): String {
        // Формируем URL для Gismeteo: https://www.gismeteo.ru/weather-{id}/
        return "https://www.gismeteo.ru/weather-${selectedCity.value.id}/${period.value.path}"
    }
}