package com.server.movile.xef.android.ui.screens.navigationdrawercompose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavController
import com.xef.xefMobile.MainLayout  // Ensure this import matches the package declaration of MainLayout
import org.xef.xefMobile.R
import com.server.movile.xef.android.ui.viewmodels.IAuthViewModel

@Composable
fun HomeScreen(authViewModel: IAuthViewModel, navController: NavController) {
    MainLayout(navController = navController, authViewModel = authViewModel) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.xef_brand_icon),
                    contentDescription = "Logo",
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Welcome to Xef.ai",
                    fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
