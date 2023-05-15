package com.xebia.functional.xef.io

import okio.FileSystem
import okio.NodeJsFileSystem

actual val FileSystem.Companion.DEFAULT: FileSystem
  get() = NodeJsFileSystem