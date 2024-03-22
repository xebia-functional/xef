package com.xebia.functional.xef.io

import okio.FileSystem

actual val FileSystem.Companion.DEFAULT: FileSystem
  get() = SYSTEM