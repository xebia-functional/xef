package com.xef.xefMobile.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.xef.xefMobile.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch
import org.xef.xefMobile.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
  // Step 1: Collect and observe the API key state from the ViewModel
  val apiKey by settingsViewModel.apiKey.collectAsState()
  var apiKeyInput by remember { mutableStateOf(apiKey) }
  val coroutineScope = rememberCoroutineScope()

  // Custom colors for UI elements
  val customColors = Color(0xFFADD8E6)
  val CustomTextBlue = Color(0xFF0199D7)

  // Step 4: Snackbar state management
  var showSnackbar by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }

  // Display Snackbar when showSnackbar becomes true
  LaunchedEffect(showSnackbar) {
    if (showSnackbar) {
      snackbarHostState.showSnackbar("API key saved successfully")
      showSnackbar = false
    }
  }

  // Step 5: Scaffold to provide structure and Snackbar host
  Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize().background(Color.White).padding(innerPadding).padding(16.dp)
    ) {
      Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Step 2: UI elements for settings screen
        Text(text = "Settings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "These are xef-server settings.", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
          value = apiKeyInput,
          onValueChange = { apiKeyInput = it },
          label = { Text("OpenAI API key") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Step 3: Button to save the API key
        Button(
          onClick = {
            coroutineScope.launch {
              settingsViewModel.setApiKey(apiKeyInput)
              settingsViewModel.saveApiKey()
              showSnackbar = true // Trigger Snackbar
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = customColors),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.save_24dp),
              contentDescription = "save settings icon",
              modifier = Modifier.size(24.dp),
              colorFilter = ColorFilter.tint(CustomTextBlue)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Save Settings", color = CustomTextBlue)
          }
        }
      }
    }
  }
}
