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

  var state by mutableStateOf(PathScreenState())
    private set

  private val uriPathFinder = UriPathFinder()

  fun onFilePathsListChange(list: List<Uri>, context: Context) {
    viewModelScope.launch {
      val updatedList = state.filePaths.toMutableList()
      val pathList = changeUriToPath(list, context)
      updatedList += pathList
      state = state.copy(filePaths = updatedList)
    }
  }

  fun removeFilePath(path: String) {
    viewModelScope.launch {
      val updatedList = state.filePaths.toMutableList()
      updatedList.remove(path)
      state = state.copy(filePaths = updatedList)
    }
  }

  private fun changeUriToPath(uris: List<Uri>, context: Context): List<String> {
    return uris.mapNotNull { uri -> uriPathFinder.getPath(context, uri) }
  }
}
