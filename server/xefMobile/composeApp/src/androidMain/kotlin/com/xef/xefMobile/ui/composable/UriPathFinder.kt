package com.xef.xefMobile.ui.composable

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore

class UriPathFinder {

  fun getPath(context: Context, uri: Uri): String? {
    return when {
      DocumentsContract.isDocumentUri(context, uri) -> {
        when {
          isExternalStorageDocument(uri) -> handleExternalStorageDocument(uri)
          isDownloadsDocument(uri) -> handleDownloadsDocument(context, uri)
          isMediaDocument(uri) -> handleMediaDocument(context, uri)
          else -> getDataColumn(context, uri, null, null)
        }
      }
      "content".equals(uri.scheme, ignoreCase = true) -> getDataColumn(context, uri, null, null)
      "file".equals(uri.scheme, ignoreCase = true) -> uri.path
      else -> null
    }
  }

  private fun handleExternalStorageDocument(uri: Uri): String? {
    val docId = DocumentsContract.getDocumentId(uri)
    val split = docId.split(":")
    val type = split[0]
    return if ("primary".equals(type, ignoreCase = true)) {
      Environment.getExternalStorageDirectory().toString() + "/" + split[1]
    } else {
      null
    }
  }

  private fun handleDownloadsDocument(context: Context, uri: Uri): String? {
    val id = DocumentsContract.getDocumentId(uri)
    return if (id.startsWith("raw:")) {
      id.removePrefix("raw:")
    } else {
      try {
        val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), id.toLong())
        getDataColumn(context, contentUri, null, null)
      } catch (e: NumberFormatException) {
        null
      }
    }
  }

  private fun handleMediaDocument(context: Context, uri: Uri): String? {
    val docId = DocumentsContract.getDocumentId(uri)
    val split = docId.split(":")
    val type = split[0]
    val id = split[1]

    val contentUri: Uri? = when (type) {
      "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
      "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
      "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
      else -> null
    }

    return contentUri?.let {
      getDataColumn(context, it, "_id=?", arrayOf(id))
    }
  }

  private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
    val cursor = uri?.let {
      context.contentResolver.query(it, arrayOf("_data"), selection, selectionArgs, null)
    }
    return cursor?.use {
      if (it.moveToFirst()) {
        val columnIndex = it.getColumnIndexOrThrow("_data")
        it.getString(columnIndex)
      } else {
        null
      }
    }
  }

  private fun isExternalStorageDocument(uri: Uri) = "com.android.externalstorage.documents" == uri.authority
  private fun isDownloadsDocument(uri: Uri) = "com.android.providers.downloads.documents" == uri.authority
  private fun isMediaDocument(uri: Uri) = "com.android.providers.media.documents" == uri.authority
}
