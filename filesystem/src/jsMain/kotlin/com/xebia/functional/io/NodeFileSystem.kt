package com.xebia.functional.io

import okio.FileSystem
import okio.NodeJsFileSystem

actual val FileSystem.Companion.DEFAULT: FileSystem
  get() = NodeJsFileSystem