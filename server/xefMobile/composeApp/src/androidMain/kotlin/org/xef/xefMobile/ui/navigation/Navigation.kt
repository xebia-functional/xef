package org.xef.xefMobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.server.movile.xef.android.ui.screens.RegisterScreen
import com.server.movile.xef.android.ui.screens.WelcomeScreen
import com.server.movile.xef.android.ui.screens.menu.AssistantScreen
import com.server.movile.xef.android.ui.screens.menu.CreateAssistantScreen
import com.server.movile.xef.android.ui.viewmodels.IAuthViewModel

@Composable
fun AppNavigator(authViewModel: IAuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "welcomeScreen") {
        composable("welcomeScreen") {
            WelcomeScreen(authViewModel = authViewModel, navController = navController)
        }
        composable("registerScreen") {
            RegisterScreen(authViewModel = authViewModel, navController = navController)
        }
        composable("startScreen") {
            //StartScreen(navController = navController)
        }
        composable("assistantScreen") {
            AssistantScreen(navController = navController)
        }
        composable("createAssistantScreen") {
            CreateAssistantScreen(navController = navController)
        }
    }
}
