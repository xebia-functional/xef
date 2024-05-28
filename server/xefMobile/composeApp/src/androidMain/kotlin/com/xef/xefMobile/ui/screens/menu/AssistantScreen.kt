package com.server.movile.xef.android.ui.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.server.movile.xef.android.ui.viewmodels.IAuthViewModel
import com.xef.xefMobile.model.Assistant
import com.xef.xefMobile.services.ApiService
import com.xef.xefMobile.theme.theme.LocalCustomColors
import com.xef.xefMobile.ui.screens.Screens
import kotlinx.coroutines.launch

@Composable
fun AssistantScreen(navController: NavController, authViewModel: IAuthViewModel) {
  val customColors = LocalCustomColors.current
  val coroutineScope = rememberCoroutineScope()
  var assistants by remember { mutableStateOf<List<Assistant>>(emptyList()) }
  var loading by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val authToken = authViewModel.authToken.value ?: error("Auth token not found")

  LaunchedEffect(Unit) {
    coroutineScope.launch {
      try {
        val response = ApiService().getAssistants(authToken)
        assistants = response.data
      } catch (e: Exception) {
        errorMessage = "Failed to load assistants"
      } finally {
        loading = false
      }
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    if (loading) {
      CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    } else if (errorMessage != null) {
      Text(
        text = errorMessage!!,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.align(Alignment.Center)
      )
    } else {
      Column(
        modifier = Modifier.align(Alignment.TopCenter).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Assistants",
          fontWeight = FontWeight.Bold,
          fontSize = 24.sp,
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

        Spacer(modifier = Modifier.height(16.dp))

        assistants.forEach { assistant ->
          Text(text = assistant.name, fontWeight = FontWeight.Bold)

          Text(text = assistant.id)
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }

    Button(
      onClick = { navController.navigate(Screens.CreateAssistant.screen) },
      colors =
        ButtonDefaults.buttonColors(
          containerColor = customColors.buttonColor,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
      modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
    ) {
      Text(text = "Create New Assistant")
    }
  }
}
