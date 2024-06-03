package com.server.movile.xef.android.ui.screens.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.server.movile.xef.android.ui.themes.LocalCustomColors
import com.server.movile.xef.android.ui.viewmodels.AssistantViewModel
import com.server.movile.xef.android.ui.viewmodels.IAuthViewModel
import com.xef.xefMobile.ui.screens.Screens
import com.xef.xefMobile.ui.viewmodels.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AssistantScreen(
  navController: NavController,
  authViewModel: IAuthViewModel,
  settingsViewModel: SettingsViewModel
) {
  val customColors = LocalCustomColors.current
  val viewModel: AssistantViewModel =
    viewModel(factory = AssistantViewModelFactory(authViewModel, settingsViewModel))
  val assistants by viewModel.assistants.collectAsState()
  val loading by viewModel.loading.collectAsState()
  val errorMessage by viewModel.errorMessage.collectAsState()

  val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

  Box(modifier = Modifier.fillMaxSize()) {
    when {
      loading -> {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
      }
      errorMessage != null -> {
        Text(
          text = errorMessage ?: "",
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.align(Alignment.Center)
        )
      }
      else -> {
        Column(
          modifier = Modifier.fillMaxSize().padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally // Align title to center
        ) {
          Text(
            text = "Assistants",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
          )
          HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

          Spacer(modifier = Modifier.height(16.dp))

          LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(assistants) { assistant ->
              Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable {
                  navController.navigate(Screens.CreateAssistantWithArgs.createRoute(assistant.id))
                },
                horizontalAlignment = Alignment.Start // Align items to start
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = assistant.name.ifBlank { "Untitled assistant" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                  )
                  Text(
                    text = sdf.format(Date(assistant.createdAt * 1000)),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterVertically)
                  )
                }
                Text(text = "ID: ${assistant.id}", fontSize = 14.sp)
              }
              HorizontalDivider(color = Color.Gray)
            }
          }
        }
      }
    }

    Button(
      onClick = { navController.navigate(Screens.CreateAssistant.screen) },
      colors = ButtonDefaults.buttonColors(
        containerColor = customColors.buttonColor,
        contentColor = MaterialTheme.colorScheme.onPrimary
      ),
      modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
    ) {
      Text(text = "Create New Assistant")
    }
  }
}

class AssistantViewModelFactory(
  private val authViewModel: IAuthViewModel,
  private val settingsViewModel: SettingsViewModel
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(AssistantViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST") return AssistantViewModel(authViewModel, settingsViewModel) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
