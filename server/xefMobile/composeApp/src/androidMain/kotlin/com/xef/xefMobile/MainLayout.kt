package com.xef.xefMobile

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
import androidx.navigation.NavController
import com.server.movile.xef.android.ui.viewmodels.IAuthViewModel
import kotlinx.coroutines.launch
import org.xef.xefMobile.R
import com.xef.xefMobile.ui.screens.Screens

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainLayout(
    navController: NavController,
    authViewModel: IAuthViewModel,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val CustomLightBlue = Color(0xFFADD8E6)
    val CustomTextBlue = Color(0xFF0199D7)
    val context = LocalContext.current

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
                HorizontalDivider()

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
                        navController.navigate(Screens.Home.screen) {
                            popUpTo(0)
                        }
                    }
                )
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
                        navController.navigate(Screens.Organizations.screen) {
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
                        navController.navigate(Screens.Assistants.screen) {
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
                        navController.navigate(Screens.Projects.screen) {
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
                        navController.navigate(Screens.Chat.screen) {
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
                        navController.navigate(Screens.GenericQuestion.screen) {
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
                        navController.navigate(Screens.Settings.screen) {
                            popUpTo(0)
                        }
                    })

                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text(text = "Logout", color = CustomTextBlue) },
                    selected = false,
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.logout_24dp_fill0_wght400_grad0_opsz24),
                            contentDescription = "logout",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(CustomTextBlue)
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        authViewModel.logout()
                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                        navController.navigate(Screens.Login.screen) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    })
            }
        },
    ) {
        Scaffold(
            topBar = {
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
            Box(modifier = Modifier.padding(it)) {
                content()
            }
        }
    }
}
