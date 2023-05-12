package com.xebia.functional.io

import okio.FileSystem

actual val FileSystem.Companion.DEFAULT: FileSystem
  get() = SYSTEM