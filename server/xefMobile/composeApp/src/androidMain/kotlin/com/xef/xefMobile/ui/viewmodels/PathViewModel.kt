package com.xef.xefMobile.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xef.xefMobile.ui.composable.UriPathFinder
import com.xef.xefMobile.ui.screens.FilePicker.PathScreenState
import kotlinx.coroutines.launch

class PathViewModel : ViewModel() {

  var fileSearchState by mutableStateOf(PathScreenState())
    private set

  var codeInterpreterState by mutableStateOf(PathScreenState())
    private set

  private val uriPathFinder = UriPathFinder()

  fun onFileSearchPathsChange(list: List<Uri>, context: Context) {
    viewModelScope.launch {
      val updatedList = fileSearchState.filePaths.toMutableList()
      val pathList = changeUriToPath(list, context)
      updatedList += pathList
      fileSearchState = fileSearchState.copy(filePaths = updatedList)
    }
  }

  fun onCodeInterpreterPathsChange(list: List<Uri>, context: Context) {
    viewModelScope.launch {
      val updatedList = codeInterpreterState.filePaths.toMutableList()
      val pathList = changeUriToPath(list, context)
      updatedList += pathList
      codeInterpreterState = codeInterpreterState.copy(filePaths = updatedList)
    }
  }

  fun removeFileSearchPath(path: String) {
    viewModelScope.launch {
      val updatedList = fileSearchState.filePaths.toMutableList()
      updatedList.remove(path)
      fileSearchState = fileSearchState.copy(filePaths = updatedList)
    }
  }

  fun removeCodeInterpreterPath(path: String) {
    viewModelScope.launch {
      val updatedList = codeInterpreterState.filePaths.toMutableList()
      updatedList.remove(path)
      codeInterpreterState = codeInterpreterState.copy(filePaths = updatedList)
    }
  }

  private fun changeUriToPath(uris: List<Uri>, context: Context): List<String> {
    return uris.mapNotNull { uri -> uriPathFinder.getPath(context, uri) }
  }
}

