package com.server.movile.xef.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.server.movile.xef.android.ui.viewmodels.IAuthViewModel
import com.xef.xefMobile.ui.screens.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(authViewModel: IAuthViewModel, navController: NavController) {
  var errorMessage by remember { mutableStateOf<String?>(null) }
  val authToken by authViewModel.authToken.observeAsState()

  LaunchedEffect(authToken) {
    if (authToken != null) {
      navController.navigate(Screens.Login.screen) {
        popUpTo(Screens.Register.screen) { inclusive = true }
      }
    }
  }

  Column(
    modifier = Modifier.fillMaxSize().background(Color.White),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "Xef Server",
      fontSize = 30.sp,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "Create an account",
      fontSize = 24.sp,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rePassword by remember { mutableStateOf("") }

    OutlinedTextField(
      value = name,
      onValueChange = { name = it },
      label = { Text("Name") },
      singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
      value = email,
      onValueChange = { email = it },
      label = { Text("Email") },
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
      value = password,
      onValueChange = { password = it },
      label = { Text("Password") },
      visualTransformation = PasswordVisualTransformation(),
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
      value = rePassword,
      onValueChange = { rePassword = it },
      label = { Text("Re-Password") },
      visualTransformation = PasswordVisualTransformation(),
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
    Spacer(modifier = Modifier.height(16.dp))
    errorMessage?.let {
      Text(text = it, color = Color.Red, style = MaterialTheme.typography.bodyLarge)
      Spacer(modifier = Modifier.height(8.dp))
    }

    Button(
      onClick = {
        errorMessage =
          when {
            name.isBlank() -> "Name is empty"
            email.isBlank() -> "Email is empty"
            password.isEmpty() -> "Password is empty"
            password != rePassword -> "Passwords do not match"
            else -> null
          }
        if (errorMessage == null) {
          authViewModel.register(name, email, password)
        }
      },
      modifier = Modifier.width(200.dp),
      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
    ) {
      Text("Create Account")
    }
    Spacer(modifier = Modifier.height(8.dp))
    TextButton(
      onClick = { navController.navigate(Screens.Login.screen) },
      modifier = Modifier.fillMaxWidth()
    ) {
      Text("Back")
    }
  }
}
