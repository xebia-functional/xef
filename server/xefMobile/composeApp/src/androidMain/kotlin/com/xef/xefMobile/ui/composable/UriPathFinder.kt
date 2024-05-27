package com.xef.xefMobile.ui.composable

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.lang.NumberFormatException

class UriPathFinder {

    fun getPath(context: Context, uri: Uri): String? {
        return when {
            DocumentsContract.isDocumentUri(context, uri) -> {
                when {
                    isExternalStorageDocument(uri) -> handleExternalStorageDocument(uri)
                    isDownloadsDocument(uri) -> handleDownloadsDocument(context, uri)
                    isMediaDocument(uri) -> handleMediaDocument(context, uri)
                    else -> null
                }
            }
            "content".equals(uri.scheme, ignoreCase = true) -> getDataColumn(context, uri, null, null)
            "file".equals(uri.scheme, ignoreCase = true) -> uri.path
            else -> null
        }
    }

    private fun handleExternalStorageDocument(uri: Uri): String? {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":").toTypedArray()
        val type = split[0]
        return if ("primary".equals(type, ignoreCase = true)) {
            Environment.getExternalStorageDirectory().toString() + "/" + split[1]
        } else {
            // Handle non-primary volumes (e.g., "content://com.android.externalstorage.documents/document/primary:...")
            val storageDefinition = System.getenv("SECONDARY_STORAGE")?.split(":")
            storageDefinition?.find { it.contains(type) }?.let { "$it/${split[1]}" }
        }
    }

    private fun handleDownloadsDocument(context: Context, uri: Uri): String? {
        val id = DocumentsContract.getDocumentId(uri)
        return try {
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"),
                java.lang.Long.valueOf(id)
            )
            getDataColumn(context, contentUri, null, null)
        } catch (e: NumberFormatException) {
            // Handle the case where the id is not a pure number
            null
        }
    }

    private fun handleMediaDocument(context: Context, uri: Uri): String? {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":").toTypedArray()
        val type = split[0]
        val contentUri: Uri? = when (type) {
            "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else -> null
        }
        val selection = "_id=?"
        val selectionArgs = arrayOf(split[1])
        return getDataColumn(context, contentUri, selection, selectionArgs)
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        val cursor: Cursor? = uri?.let {
            context.contentResolver.query(it, arrayOf("_data"), selection, selectionArgs, null)
        }
        return cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex: Int = it.getColumnIndexOrThrow("_data")
                it.getString(columnIndex)
            } else {
                null
            }
        }
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}
