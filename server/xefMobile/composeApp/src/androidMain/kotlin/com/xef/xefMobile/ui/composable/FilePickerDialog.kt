package com.xef.xefMobile.ui.composable

import android.content.Intent
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
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.server.movile.xef.android.ui.themes.CustomColors
import com.xef.xefMobile.ui.viewmodels.PathViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FilePickerDialog(
  onDismissRequest: () -> Unit,
  customColors: CustomColors,
  onFilesSelected: () -> Unit,
  mimeTypeFilter: String = "*/*",
  isForCodeInterpreter: Boolean = false
) {
  val viewModel: PathViewModel = viewModel()
  val state =
    if (isForCodeInterpreter) viewModel.codeInterpreterState else viewModel.fileSearchState
  val context = LocalContext.current

  val permissions =
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      listOf(
        android.Manifest.permission.READ_MEDIA_IMAGES,
        android.Manifest.permission.READ_MEDIA_VIDEO,
        android.Manifest.permission.READ_MEDIA_AUDIO
      )
    } else {
      listOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

  val permissionState = rememberMultiplePermissionsState(permissions)

  var selectedFile by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) { permissionState.launchMultiplePermissionRequest() }

  val filePickerLauncher =
    rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
      uri?.let {
        val takeFlags: Int =
          Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(it, takeFlags)
        if (isForCodeInterpreter) {
          viewModel.onCodeInterpreterPathsChange(listOf(it), context)
        } else {
          viewModel.onFileSearchPathsChange(listOf(it), context)
        }
        onFilesSelected()
        selectedFile = state.filePaths.firstOrNull()
      }
    }

  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Selected Files", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
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
            if (permissionState.allPermissionsGranted) {
              filePickerLauncher.launch(arrayOf(mimeTypeFilter))
            } else {
              permissionState.launchMultiplePermissionRequest()
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
              if (isForCodeInterpreter) {
                viewModel.removeCodeInterpreterPath(selectedFile!!)
              } else {
                viewModel.removeFileSearchPath(selectedFile!!)
              }
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
