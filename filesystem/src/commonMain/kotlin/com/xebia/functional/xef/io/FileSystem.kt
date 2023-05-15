package com.xebia.functional.xef.io

import okio.FileSystem

/** Common definition for accessing the default FileSystem for Okio. */
expect val FileSystem.Companion.DEFAULT: FileSystem
