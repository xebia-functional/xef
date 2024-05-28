package com.xef.xefMobile.ui.composable

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.server.movile.xef.android.ui.themes.CustomColors
import com.xef.xefMobile.ui.viewmodels.PathViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FilePickerDialog(
  onDismissRequest: () -> Unit,
  customColors: CustomColors,
  onFilesSelected: () -> Unit
) {
  val viewModel: PathViewModel = viewModel()
  val state = viewModel.state
  val context = LocalContext.current

  val permissionState =
    rememberPermissionState(permission = android.Manifest.permission.READ_EXTERNAL_STORAGE)

  var selectedFile by remember { mutableStateOf<String?>(null) }

  SideEffect {
    if (!permissionState.status.isGranted) {
      permissionState.launchPermissionRequest()
    }
  }

  val filePickerLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.GetMultipleContents(),
      onResult = { uris ->
        viewModel.onFilePathsListChange(uris, context)
        if (uris.isNotEmpty()) {
          onFilesSelected() // Call the callback when files are selected
          selectedFile = state.filePaths.firstOrNull()
        }
      }
    )

  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Selected Files", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxSize().padding(15.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier.fillMaxWidth().fillMaxHeight(0.76f),
          contentAlignment = Alignment.Center
        ) {
          if (state.filePaths.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text(text = "No files selected")
            }
          } else {
            LazyColumn {
              items(state.filePaths) { path ->
                Text(
                  text = path,
                  modifier =
                    Modifier.fillMaxWidth().clickable { selectedFile = path }.padding(8.dp),
                  color = if (selectedFile == path) Color.Blue else Color.Unspecified
                )
              }
            }
          }
        }
        OutlinedButton(
          onClick = {
            if (permissionState.status.isGranted) {
              filePickerLauncher.launch("*/*")
            } else {
              permissionState.launchPermissionRequest()
            }
          },
          colors =
            ButtonDefaults.outlinedButtonColors(
              containerColor = Color.Transparent,
              contentColor = customColors.buttonColor
            )
        ) {
          Text(text = "Browse files")
        }
        if (selectedFile != null) {
          OutlinedButton(
            onClick = {
              viewModel.removeFilePath(selectedFile!!)
              selectedFile = null
            },
            colors =
              ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = customColors.buttonColor
              )
          ) {
            Text(text = "Remove")
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onDismissRequest() },
        colors =
          ButtonDefaults.buttonColors(
            containerColor = customColors.buttonColor,
            contentColor = MaterialTheme.colorScheme.onPrimary
          )
      ) {
        Text("Done")
      }
    }
  )
}
