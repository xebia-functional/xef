package com.server.movile.xef.android.ui.screens.menu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.server.movile.xef.android.ui.themes.LocalCustomColors
import com.xef.xefMobile.ui.composable.FilePickerDialog

class CreateAssistantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            CreateAssistantScreen(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssistantScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf(1f) }
    var topP by remember { mutableStateOf(1f) }
    var fileSearchEnabled by remember { mutableStateOf(false) }
    var codeInterpreterEnabled by remember { mutableStateOf(false) }
    var model by remember { mutableStateOf("gpt-4-turbo") }
    val list = listOf("gpt-4o", "gpt-4", "gpt-3.5-turbo-16K", "gpt-3.5-turbo-0125", "gpt-3.5-turbo")
    var isExpanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(list[0]) }
    var showFilePicker by remember { mutableStateOf(false) }

    val customColors = LocalCustomColors.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Assistant",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instructions") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        value = selectedText,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                        }
                    )
                    ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
                        list.forEachIndexed { index, text ->
                            DropdownMenuItem(
                                text = { Text(text = text) },
                                onClick = {
                                    selectedText = list[index]
                                    isExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "TOOLS")

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { showFilePicker = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = customColors.buttonColor
                    )
                ) {
                    Text("File Search +")
                }
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = fileSearchEnabled,
                    onCheckedChange = { fileSearchEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = customColors.sliderThumbColor,
                        checkedTrackColor = customColors.sliderTrackColor
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { /* handle cancel */ },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = customColors.buttonColor
                    )
                ) {
                    Text("Code Interpreter +")
                }
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = codeInterpreterEnabled,
                    onCheckedChange = { codeInterpreterEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = customColors.sliderThumbColor,
                        checkedTrackColor = customColors.sliderTrackColor
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { /* handle cancel */ },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = customColors.buttonColor
                    )
                ) {
                    Text("Functions +")
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "MODEL CONFIGURATION")

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            AssistantFloatField(label = "Temperature", value = temperature, onValueChange = { temperature = it })

            AssistantFloatField(label = "Top P", value = topP, onValueChange = { topP = it })

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { navController.navigateUp() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.buttonColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { /* handle create */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.buttonColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Create")
                }
            }
        }
        if (showFilePicker) {
            FilePickerDialog(
                onDismissRequest = { showFilePicker = false },
                customColors = customColors,
                onFilesSelected = {
                    // Handle file selection here if needed
                    showFilePicker = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantFloatField(label: String, value: Float, onValueChange: (Float) -> Unit) {
    val customColors = LocalCustomColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(bottom = 2.dp)  // Reduce padding for the label
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..2f,
                steps = 100,  // This ensures the slider moves in increments of 0.02
                modifier = Modifier.weight(3f),
                colors = SliderDefaults.colors(
                    thumbColor = customColors.sliderThumbColor,
                    activeTrackColor = customColors.sliderTrackColor
                )
            )
            Spacer(modifier = Modifier.width(2.dp))  // Add a small spacer between the slider and text field
            TextField(
                value = String.format("%.2f", value),
                onValueChange = {
                    val newValue = it.toFloatOrNull() ?: 0f
                    onValueChange(newValue)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(60.dp)
                    .height(50.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)  // Optionally adjust text size
            )
        }
    }
}

@Preview(showBackground = false)
@Composable
fun CreateAssistantScreenPreview() {
    val navController = rememberNavController()
    CreateAssistantScreen(navController)
}
