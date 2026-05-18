package com.example.travelcompanion

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.travelcompanion.ui.theme.TravelCompanionTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelCompanionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TravelCompanionApp()
                }
            }
        }
    }
}

enum class ConversionCategory(
    val label: String,
    val emoji: String,
    val units: List<String>
) {
    CURRENCY(
        label = "Currency",
        emoji = "💱",
        units = listOf("USD", "AUD", "EUR", "JPY", "GBP")
    ),
    FUEL_TRAVEL(
        label = "Fuel & Travel",
        emoji = "⛽",
        units = listOf("MPG", "km/L", "Gallon (US)", "Liter", "Nautical Mile", "Kilometer")
    ),
    TEMPERATURE(
        label = "Temperature",
        emoji = "🌡️",
        units = listOf("Celsius", "Fahrenheit", "Kelvin")
    )
}

object Converter {
    private val currencyToUsd = mapOf(
        "USD" to 1.0,
        "AUD" to 1.0 / 1.55,
        "EUR" to 1.0 / 0.92,
        "JPY" to 1.0 / 148.50,
        "GBP" to 1.0 / 0.78
    )

    fun convert(category: ConversionCategory, from: String, to: String, value: Double): Result<Double> {
        if (from == to) return Result.success(value)

        return when (category) {
            ConversionCategory.CURRENCY -> convertCurrency(from, to, value)
            ConversionCategory.FUEL_TRAVEL -> convertFuelAndTravel(from, to, value)
            ConversionCategory.TEMPERATURE -> convertTemperature(from, to, value)
        }
    }

    private fun convertCurrency(from: String, to: String, value: Double): Result<Double> {
        val fromRate = currencyToUsd[from] ?: return Result.failure(IllegalArgumentException("Unsupported source currency"))
        val toRate = currencyToUsd[to] ?: return Result.failure(IllegalArgumentException("Unsupported target currency"))

        val usdValue = value * fromRate
        val converted = usdValue / toRate
        return Result.success(converted)
    }

    private fun convertFuelAndTravel(from: String, to: String, value: Double): Result<Double> {
        if (value < 0) {
            return Result.failure(IllegalArgumentException("Negative values are not allowed for fuel or travel measurements."))
        }

        return when {
            from in fuelEfficiencyUnits() && to in fuelEfficiencyUnits() -> {
                val converted = when {
                    from == "MPG" && to == "km/L" -> value * 0.425
                    from == "km/L" && to == "MPG" -> value / 0.425
                    else -> value
                }
                Result.success(converted)
            }

            from in volumeUnits() && to in volumeUnits() -> {
                val converted = when {
                    from == "Gallon (US)" && to == "Liter" -> value * 3.785
                    from == "Liter" && to == "Gallon (US)" -> value / 3.785
                    else -> value
                }
                Result.success(converted)
            }

            from in distanceUnits() && to in distanceUnits() -> {
                val converted = when {
                    from == "Nautical Mile" && to == "Kilometer" -> value * 1.852
                    from == "Kilometer" && to == "Nautical Mile" -> value / 1.852
                    else -> value
                }
                Result.success(converted)
            }

            else -> Result.failure(
                IllegalArgumentException(
                    "Choose units from the same measurement type inside Fuel & Travel."
                )
            )
        }
    }

    private fun convertTemperature(from: String, to: String, value: Double): Result<Double> {
        val celsius = when (from) {
            "Celsius" -> value
            "Fahrenheit" -> (value - 32) / 1.8
            "Kelvin" -> value - 273.15
            else -> return Result.failure(IllegalArgumentException("Unsupported source temperature unit"))
        }

        val converted = when (to) {
            "Celsius" -> celsius
            "Fahrenheit" -> (celsius * 1.8) + 32
            "Kelvin" -> celsius + 273.15
            else -> return Result.failure(IllegalArgumentException("Unsupported target temperature unit"))
        }

        return Result.success(converted)
    }

    private fun fuelEfficiencyUnits() = setOf("MPG", "km/L")
    private fun volumeUnits() = setOf("Gallon (US)", "Liter")
    private fun distanceUnits() = setOf("Nautical Mile", "Kilometer")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TravelCompanionApp() {
    val context = LocalContext.current
    val categories = ConversionCategory.entries

    var selectedCategory by rememberSaveable { mutableStateOf(ConversionCategory.CURRENCY) }
    var fromUnit by rememberSaveable { mutableStateOf(selectedCategory.units.first()) }
    var toUnit by rememberSaveable { mutableStateOf(selectedCategory.units.getOrElse(1) { selectedCategory.units.first() }) }
    var inputValue by rememberSaveable { mutableStateOf("") }
    var resultText by rememberSaveable { mutableStateOf("Converted value will appear here") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Travel Companion",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Convert travel essentials for currency, fuel and temperature.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Conversion Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        AssistChip(
                            onClick = {
                                selectedCategory = category
                                fromUnit = category.units.first()
                                toUnit = category.units.getOrElse(1) { category.units.first() }
                                resultText = "Converted value will appear here"
                            },
                            label = { Text("${category.emoji} ${category.label}") }
                        )
                    }
                }
            }
        }

        CategoryBanner(selectedCategory)

        UnitDropdown(
            label = "From",
            options = selectedCategory.units,
            selectedOption = fromUnit,
            onOptionSelected = { fromUnit = it }
        )

        UnitDropdown(
            label = "To",
            options = selectedCategory.units,
            selectedOption = toUnit,
            onOptionSelected = { toUnit = it }
        )

        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            label = { Text("Value to convert") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Button(
            onClick = {
                val parsedValue = inputValue.toDoubleOrNull()
                when {
                    inputValue.isBlank() -> {
                        Toast.makeText(context, "Enter a value first.", Toast.LENGTH_SHORT).show()
                    }
                    parsedValue == null -> {
                        Toast.makeText(context, "Only numeric values can be converted.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val conversionResult = Converter.convert(selectedCategory, fromUnit, toUnit, parsedValue)
                        conversionResult
                            .onSuccess { convertedValue ->
                                val formattedInput = formatNumber(parsedValue)
                                val formattedOutput = formatNumber(convertedValue)
                                resultText = "$formattedInput $fromUnit = $formattedOutput $toUnit"
                                if (fromUnit == toUnit) {
                                    Toast.makeText(context, "Source and destination are the same.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .onFailure { error ->
                                Toast.makeText(context, error.message ?: "Conversion failed.", Toast.LENGTH_LONG).show()
                            }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Convert")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Result",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Included Task Rates & Formulas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text("• Currency uses the fixed 2026 task rates provided in the brief.")
                Text("• MPG ↔ km/L uses 1 MPG = 0.425 km/L.")
                Text("• Gallon (US) ↔ Liter uses 1 gallon = 3.785 liters.")
                Text("• Nautical Mile ↔ Kilometer uses 1 nautical mile = 1.852 km.")
                Text("• Temperature converts across Celsius, Fahrenheit and Kelvin.")
            }
        }
    }
}

@Composable
fun CategoryBanner(category: ConversionCategory) {
    val bannerColor = when (category) {
        ConversionCategory.CURRENCY -> Color(0xFFE8F5E9)
        ConversionCategory.FUEL_TRAVEL -> Color(0xFFFFF3E0)
        ConversionCategory.TEMPERATURE -> Color(0xFFE3F2FD)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bannerColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color.White, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.emoji, fontSize = 22.sp)
        }

        Column {
            Text(
                text = category.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when (category) {
                    ConversionCategory.CURRENCY -> "Convert major currencies using the task's fixed rates."
                    ConversionCategory.FUEL_TRAVEL -> "Convert fuel efficiency, liquid volume and travel distance."
                    ConversionCategory.TEMPERATURE -> "Check climate values across Celsius, Fahrenheit and Kelvin."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun formatNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        String.format(Locale.US, "%.0f", value)
    } else {
        String.format(Locale.US, "%.4f", value)
            .trimEnd('0')
            .trimEnd('.')
    }
}
