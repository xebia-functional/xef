package com.xebia.functional.xef.io

import okio.FileSystem
import okio.Path

/** Common definition for accessing the default FileSystem for Okio. */
expect val FileSystem.Companion.DEFAULT: FileSystem

fun FileSystem.isDirectory(path: Path) = metadataOrNull(path)?.isDirectory == true
fun FileSystem.isRegularFile(path: Path) = metadataOrNull(path)?.isRegularFile == true
