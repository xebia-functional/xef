package com.xef.xefMobile

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.server.movile.xef.android.ui.screens.LoginScreen
import com.server.movile.xef.android.ui.screens.RegisterScreen
import com.server.movile.xef.android.ui.screens.menu.AssistantScreen
import com.server.movile.xef.android.ui.screens.menu.CreateAssistantScreen
import com.server.movile.xef.android.ui.screens.navigationdrawercompose.HomeScreen
import com.server.movile.xef.android.ui.viewmodels.IAuthViewModel
import com.xef.xefMobile.ui.screens.Screens
import com.xef.xefMobile.ui.screens.SettingsScreen
import com.xef.xefMobile.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun XefAndroidApp(authViewModel: IAuthViewModel, settingsViewModel: SettingsViewModel) {
  val navController = rememberNavController()
  val userName by authViewModel.userName.observeAsState("")

  NavHost(
    navController = navController,
    startDestination = Screens.Login.screen,
    modifier = Modifier.padding(top = 16.dp)
  ) {
    composable(Screens.Login.screen) { LoginScreen(authViewModel, navController) }
    composable(Screens.Register.screen) { RegisterScreen(authViewModel, navController) }
    composable(Screens.Home.screen) {
      MainLayout(
        navController = navController,
        authViewModel = authViewModel,
        userName = userName.orEmpty()
      ) {
        HomeScreen(authViewModel, navController)
      }
    }
    composable(Screens.Assistants.screen) {
      MainLayout(
        navController = navController,
        authViewModel = authViewModel,
        userName = userName.orEmpty()
      ) {
        AssistantScreen(navController, authViewModel, settingsViewModel)
      }
    }
    composable(
      route = Screens.CreateAssistantWithArgs.screen,
      arguments = listOf(navArgument("assistantId") { type = NavType.StringType })
    ) { backStackEntry ->
      val assistantId = backStackEntry.arguments?.getString("assistantId")
      MainLayout(
        navController = navController,
        authViewModel = authViewModel,
        userName = userName.orEmpty()
      ) {
        CreateAssistantScreen(
          navController = navController,
          authViewModel = authViewModel,
          settingsViewModel = settingsViewModel,
          assistantId = assistantId
        )
      }
    }
    composable(Screens.CreateAssistant.screen) {
      MainLayout(
        navController = navController,
        authViewModel = authViewModel,
        userName = userName.orEmpty()
      ) {
        CreateAssistantScreen(
          navController = navController,
          authViewModel = authViewModel,
          settingsViewModel = settingsViewModel,
          assistantId = null
        )
      }
    }
    composable(Screens.Settings.screen) {
      MainLayout(
        navController = navController,
        authViewModel = authViewModel,
        userName = userName.orEmpty()
      ) {
        SettingsScreen(navController, settingsViewModel)
      }
    }
    // Add other composable screens here...
  }
}
