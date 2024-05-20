package org.xef.xefMobile

import HomeScreen
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.server.movile.xef.android.ui.screens.Screens
import com.server.movile.xef.android.ui.screens.StartScreen
import kotlinx.coroutines.launch
import com.server.movile.xef.android.ui.screens.menu.AssistantScreen
import com.server.movile.xef.android.ui.screens.menu.CreateAssistantScreen
import org.xef.xefMobile.R

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun XefAndroidApp() {
    val navigationController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current.applicationContext
    val CustomLightBlue = Color(0xFFADD8E6)
    val CustomLighterBlue = Color(0xFFB0E0E6)
    val CustomTextBlue = Color(0xFF0199D7)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .background(CustomLightBlue)
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.xef_brand_name),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.Center)
                    )
                }
                Divider()
                NavigationDrawerItem(
                    label = { Text(text = "Home", color = CustomTextBlue) },
                    selected = false,
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.home_24px),
                            contentDescription = "home",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(CustomTextBlue)
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.Home.screen) {
                            popUpTo(0)
                        }
                    })
                NavigationDrawerItem(
                    label = { Text(text = "Organizations", color = CustomTextBlue) },
                    selected = false,
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.source_environment_24px),
                            contentDescription = "organizations",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(CustomTextBlue)
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.Organizations.screen) {
                            popUpTo(0)
                        }
                    })
                NavigationDrawerItem(
                    label = { Text(text = "Assistants", color = CustomTextBlue) },
                    selected = false,
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.smart_toy_24px),
                            contentDescription = "assistants",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(CustomTextBlue)
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.Assistants.screen) {
                            popUpTo(0)
                        }
                    })
                NavigationDrawerItem(
                    label = { Text(text = "Projects", color = CustomTextBlue) },
                    selected = false,
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.school_24px),
                            contentDescription = "projects",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(CustomTextBlue)
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.Projects.screen) {
                            popUpTo(0)
                        }
                    })
                NavigationDrawerItem(
                    label = { Text(text = "Chat", color = CustomTextBlue) },
                    selected = false,
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.chat_24px),
                            contentDescription = "chat",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(CustomTextBlue)
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.Chat.screen) {
                            popUpTo(0)
                        }
                    })
                NavigationDrawerItem(
                    label = { Text(text = "Generic question", color = CustomTextBlue) },
                    selected = false,
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.support_agent_24px),
                            contentDescription = "generic question",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(CustomTextBlue)
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.GenericQuestion.screen) {
                            popUpTo(0)
                        }
                    })
                NavigationDrawerItem(
                    label = { Text(text = "Settings", color = CustomTextBlue) },
                    selected = false,
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.settings_24px),
                            contentDescription = "settings",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(CustomTextBlue)
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.Settings.screen) {
                            popUpTo(0)
                        }
                        Toast.makeText(context, "Logout", Toast.LENGTH_SHORT).show()
                    })
            }
        },
    ) {
        Scaffold(
            topBar = {
                val coroutineScope = rememberCoroutineScope()
                TopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.xef_brand_name_white),
                            contentDescription = "Logo",
                            modifier = Modifier.size(60.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CustomLightBlue,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }

                        }) {
                            Icon(
                                Icons.Rounded.Menu, contentDescription = "MenuButton"
                            )
                        }
                    },
                )
            }
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))  // Adjust the height as needed
                NavHost(
                    navController = navigationController,
                    startDestination = Screens.Home.screen,
                    modifier = Modifier.padding(top = 16.dp)  // Ensure there's some padding at the top
                ) {
                    composable(Screens.Start.screen) { StartScreen() }
                    composable(Screens.Home.screen) { HomeScreen() }
                    composable(Screens.Organizations.screen) { }
                    composable(Screens.Assistants.screen) { AssistantScreen(navigationController) }
                    composable(Screens.CreateAssistant.screen) { CreateAssistantScreen(navigationController) }
                    composable(Screens.Projects.screen) { }
                    composable(Screens.Chat.screen) { }
                    composable(Screens.GenericQuestion.screen) { }
                    composable(Screens.Settings.screen) { }
                }
            }
        }
    }
}
