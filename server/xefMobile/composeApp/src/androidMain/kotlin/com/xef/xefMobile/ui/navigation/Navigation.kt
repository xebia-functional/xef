package com.xef.xefMobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.server.movile.xef.android.ui.screens.LoginScreen
import com.server.movile.xef.android.ui.screens.RegisterScreen
import com.server.movile.xef.android.ui.screens.menu.AssistantScreen
import com.server.movile.xef.android.ui.screens.menu.CreateAssistantScreen
import com.server.movile.xef.android.ui.viewmodels.IAuthViewModel
import com.xef.xefMobile.ui.screens.Screens

@Composable
fun AppNavigator(authViewModel: IAuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screens.Login.screen) {
        composable(Screens.Login.screen) {
            LoginScreen(authViewModel = authViewModel, navController = navController)
        }
        composable(Screens.Register.screen) {
            RegisterScreen(authViewModel = authViewModel, navController = navController)
        }
        composable(Screens.Assistants.screen) {
            AssistantScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(Screens.CreateAssistant.screen) {
            CreateAssistantScreen(navController = navController)
        }
    }
}