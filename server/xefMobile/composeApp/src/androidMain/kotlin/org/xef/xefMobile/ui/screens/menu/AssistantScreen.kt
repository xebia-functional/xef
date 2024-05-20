package com.server.movile.xef.android.ui.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.server.movile.xef.android.ui.screens.Screens
import org.xef.xefMobile.theme.LocalCustomColors

@Composable
fun AssistantScreen(navController: NavController) {
    val customColors = LocalCustomColors.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(45.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Assistants",
                fontSize = 24.sp,
            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        // Button at the bottom center
        Button(
            onClick = { navController.navigate(Screens.CreateAssistant.screen) },
            colors = ButtonDefaults.buttonColors(
                containerColor = customColors.buttonColor,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(text = "Create New Assistant")
        }
    }
}

