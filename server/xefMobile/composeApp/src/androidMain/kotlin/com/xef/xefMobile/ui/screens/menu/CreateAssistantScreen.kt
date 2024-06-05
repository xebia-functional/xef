package com.server.movile.xef.android.ui.screens.menu

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.server.movile.xef.android.ui.themes.CustomColors
import com.server.movile.xef.android.ui.themes.LocalCustomColors
import com.server.movile.xef.android.ui.viewmodels.AssistantViewModel
import com.server.movile.xef.android.ui.viewmodels.AuthViewModel
import com.server.movile.xef.android.ui.viewmodels.IAuthViewModel
import com.server.movile.xef.android.ui.viewmodels.factory.AuthViewModelFactory
import com.xef.xefMobile.ui.composable.FilePickerDialog
import com.xef.xefMobile.ui.screens.Screens
import com.xef.xefMobile.ui.viewmodels.SettingsViewModel
import com.xef.xefMobile.ui.viewmodels.SettingsViewModelFactory
import kotlinx.coroutines.launch
import org.xef.xefMobile.R

class CreateAssistantActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val assistantId = intent.getStringExtra("assistantId")
    setContent {
      val navController = rememberNavController()

      val authViewModel: AuthViewModel =
        viewModel(factory = AuthViewModelFactory(applicationContext))
      val settingsViewModel: SettingsViewModel =
        viewModel(factory = SettingsViewModelFactory(applicationContext))

      CreateAssistantScreen(navController, authViewModel, settingsViewModel, assistantId)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssistantScreen(
  navController: NavController,
  authViewModel: IAuthViewModel,
  settingsViewModel: SettingsViewModel,
  assistantId: String?
) {
  val viewModel: AssistantViewModel =
    viewModel(factory = AssistantViewModelFactory(authViewModel, settingsViewModel))
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  val selectedAssistant by viewModel.selectedAssistant.collectAsState(initial = null)

  var name by remember { mutableStateOf("") }
  var instructions by remember { mutableStateOf("") }
  var temperature by remember { mutableStateOf(1f) }
  var topP by remember { mutableStateOf(1f) }
  var fileSearchEnabled by remember { mutableStateOf(false) }
  var codeInterpreterEnabled by remember { mutableStateOf(false) }
  var model by remember { mutableStateOf("gpt-4-turbo") }
  val list =
    listOf(
      "gpt-4o",
      "gpt-4o-2024-05-13",
      "gpt-4",
      "gpt-4-vision-preview",
      "gpt-4-turbo-preview",
      "gpt-4-2024-04-09",
      "gpt-4-turbo",
      "gpt-4-1106-preview",
      "gpt-4-0613",
      "gpt-4-0125-preview",
      "gpt-4",
      "gpt-3.5-turbo-16K",
      "gpt-3.5-turbo-0125",
      "gpt-3.5-turbo"
    )
  var isExpanded by remember { mutableStateOf(false) }
  var selectedText by remember { mutableStateOf(list[0]) }
  var showFilePicker by remember { mutableStateOf(false) }
  var showCodeInterpreterPicker by remember { mutableStateOf(false) }
  var showAllItems by remember { mutableStateOf(false) }

  val customColors = LocalCustomColors.current

  LaunchedEffect(assistantId) {
    if (assistantId != null) {
      Log.d("CreateAssistantScreen", "Loading assistant details for id: $assistantId")
      viewModel.loadAssistantDetails(assistantId)
    }
  }

  LaunchedEffect(selectedAssistant) {
    Log.d("CreateAssistantScreen", "Selected assistant changed: $selectedAssistant")
    selectedAssistant?.let { assistant ->
      name = assistant.name
      instructions = assistant.instructions
      temperature = assistant.temperature
      topP = assistant.topP
      model = assistant.model
      selectedText = assistant.model
      fileSearchEnabled = assistant.tools.any { it.type == "file_search" }
      codeInterpreterEnabled = assistant.tools.any { it.type == "code_interpreter" }
    }
  }

  Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, modifier = Modifier.fillMaxSize()) {
    paddingValues ->
    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
      LazyColumn(
        modifier = Modifier.padding(8.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        item {
          Text(
            text = "Create Assistant",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
          )
        }

        item {
          Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = name,
              onValueChange = { name = it },
              label = { Text("Name") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
          Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = instructions,
              onValueChange = { instructions = it },
              label = { Text("Instructions") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
          Box(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
              expanded = isExpanded,
              onExpandedChange = { isExpanded = !isExpanded },
              modifier = Modifier.fillMaxWidth()
            ) {
              OutlinedTextField(
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) }
              )
              ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
              ) {
                val itemsToShow = if (showAllItems) list else list.take(5)
                itemsToShow.forEachIndexed { index, text ->
                  DropdownMenuItem(
                    text = { Text(text = text) },
                    onClick = {
                      selectedText = list[index]
                      model = list[index]
                      isExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                  )
                }
                if (!showAllItems) {
                  DropdownMenuItem(
                    text = { Text(text = "Show more", color = Color.Red) },
                    onClick = { showAllItems = true },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                  )
                }
              }
            }
          }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
          ToolsSection(
            showFilePicker = showFilePicker,
            onShowFilePickerChange = { showFilePicker = it },
            fileSearchEnabled = fileSearchEnabled,
            onFileSearchEnabledChange = { fileSearchEnabled = it },
            showCodeInterpreterPicker = showCodeInterpreterPicker,
            onShowCodeInterpreterPickerChange = { showCodeInterpreterPicker = it },
            codeInterpreterEnabled = codeInterpreterEnabled,
            onCodeInterpreterEnabledChange = { codeInterpreterEnabled = it },
            customColors = customColors
          )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
          Column {
            Text(text = "MODEL CONFIGURATION")
            HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
          }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        item {
          AssistantFloatField(
            label = "Temperature",
            value = temperature,
            onValueChange = { temperature = it },
            valueRange = 0f..2f
          )
        }

        item {
          AssistantFloatField(
            label = "Top P",
            value = topP,
            onValueChange = { topP = it },
            valueRange = 0f..1f
          )
        }

        item {
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
              Button(
                onClick = { navController.navigateUp() },
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = customColors.buttonColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                  )
              ) {
                Text("Cancel")
              }
              Spacer(modifier = Modifier.width(8.dp))
              Button(
                onClick = {
                  coroutineScope.launch {
                    viewModel.createAssistant(
                      name = name,
                      instructions = instructions,
                      temperature = temperature,
                      topP = topP,
                      model = selectedText,
                      fileSearchEnabled = fileSearchEnabled,
                      codeInterpreterEnabled = codeInterpreterEnabled,
                      onSuccess = {
                        coroutineScope.launch {
                          snackbarHostState.showSnackbar("Assistant created successfully")
                          navController.navigate(Screens.Assistants.screen)
                        }
                      },
                      onError = { errorMessage ->
                        Log.e("CreateAssistantScreen", errorMessage)
                        coroutineScope.launch { snackbarHostState.showSnackbar(errorMessage) }
                      }
                    )
                  }
                },
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = customColors.buttonColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                  )
              ) {
                Text("Create")
              }
            }
            if (assistantId != null) {
              IconButton(
                onClick = {
                  coroutineScope.launch {
                    viewModel.deleteAssistant(
                      assistantId,
                      onSuccess = {
                        coroutineScope.launch {
                          snackbarHostState.showSnackbar("Assistant deleted successfully")
                          navController.navigate(Screens.Assistants.screen)
                        }
                      },
                      onError = { errorMessage ->
                        Log.e("CreateAssistantScreen", errorMessage)
                        coroutineScope.launch { snackbarHostState.showSnackbar(errorMessage) }
                      }
                    )
                  }
                },
                modifier = Modifier.size(48.dp).clip(CircleShape),
                colors =
                  IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                  )
              ) {
                Icon(
                  painter = painterResource(id = R.drawable.delete_24dp),
                  contentDescription = "Delete Assistant",
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
          }
        }
      }
    }

    if (showFilePicker) {
      FilePickerDialog(
        onDismissRequest = { showFilePicker = false },
        customColors = customColors,
        onFilesSelected = { showFilePicker = false }
      )
    }

    if (showCodeInterpreterPicker) {
      FilePickerDialog(
        onDismissRequest = { showCodeInterpreterPicker = false },
        customColors = customColors,
        onFilesSelected = { showCodeInterpreterPicker = false },
        mimeTypeFilter = "text/*"
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantFloatField(
  label: String,
  value: Float,
  onValueChange: (Float) -> Unit,
  valueRange: ClosedFloatingPointRange<Float>
) {
  val customColors = LocalCustomColors.current
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(text = label, modifier = Modifier.padding(bottom = 2.dp))
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = 200,
        modifier = Modifier.weight(3f),
        colors =
          SliderDefaults.colors(
            thumbColor = customColors.sliderThumbColor,
            activeTrackColor = customColors.sliderTrackColor
          )
      )
      Spacer(modifier = Modifier.width(2.dp))
      TextField(
        value = String.format("%.2f", value),
        onValueChange = {
          val newValue = it.toFloatOrNull() ?: 0f
          onValueChange(newValue)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.width(60.dp).height(50.dp),
        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
      )
    }
  }
}

@Composable
fun ExpandableContent(
  expanded: Boolean,
  onExpandedChange: (Boolean) -> Unit,
  header: @Composable () -> Unit,
  content: @Composable () -> Unit
) {
  Column {
    header()
    if (expanded) {
      content()
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsSection(
  showFilePicker: Boolean,
  onShowFilePickerChange: (Boolean) -> Unit,
  fileSearchEnabled: Boolean,
  onFileSearchEnabledChange: (Boolean) -> Unit,
  showCodeInterpreterPicker: Boolean,
  onShowCodeInterpreterPickerChange: (Boolean) -> Unit,
  codeInterpreterEnabled: Boolean,
  onCodeInterpreterEnabledChange: (Boolean) -> Unit,
  customColors: CustomColors
) {
  var expanded by remember { mutableStateOf(false) }

  ExpandableContent(
    expanded = expanded,
    onExpandedChange = { expanded = it },
    header = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "TOOLS")
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { expanded = !expanded }) {
          Icon(
            imageVector =
              if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (expanded) "Collapse" else "Expand"
          )
        }
        HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(top = 10.dp))
      }
    }
  ) {
    Column {
      Spacer(modifier = Modifier.height(8.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(
          onClick = { onShowFilePickerChange(true) },
          colors =
            ButtonDefaults.outlinedButtonColors(
              containerColor = Color.Transparent,
              contentColor = customColors.buttonColor
            )
        ) {
          Text("File Search +")
        }
        Spacer(modifier = Modifier.weight(1f))
        Switch(
          checked = fileSearchEnabled,
          onCheckedChange = onFileSearchEnabledChange,
          colors =
            SwitchDefaults.colors(
              checkedThumbColor = customColors.sliderThumbColor,
              checkedTrackColor = customColors.sliderTrackColor
            )
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(
          onClick = { onShowCodeInterpreterPickerChange(true) },
          colors =
            ButtonDefaults.outlinedButtonColors(
              containerColor = Color.Transparent,
              contentColor = customColors.buttonColor
            )
        ) {
          Text("Code Interpreter +")
        }
        Spacer(modifier = Modifier.weight(1f))
        Switch(
          checked = codeInterpreterEnabled,
          onCheckedChange = onCodeInterpreterEnabledChange,
          colors =
            SwitchDefaults.colors(
              checkedThumbColor = customColors.sliderThumbColor,
              checkedTrackColor = customColors.sliderTrackColor
            )
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
    }
  }
}
