package com.xebia.functional.xef.treesitter

import com.sun.jna.Pointer
import com.xebia.functional.xef.io.CommandExecutor
import com.xebia.functional.xef.io.DEFAULT
import com.xebia.functional.xef.io.Platform
import com.xebia.functional.xef.io.download
import io.github.oshai.KotlinLogging
import okio.FileSystem
import okio.Path
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream


private val logger = KotlinLogging.logger {}

suspend fun FileSystem.installTreeSitter(location: Path) {
  val treeSitterPath = location.resolve("libtree-sitter.dylib")
  if (treeSitterPath.toFile().exists()) {
    logger.debug { "🌲 tree-sitter already installed at $treeSitterPath" }
    return
  }
  logger.debug { "🌲 Installing tree-sitter to $treeSitterPath" }
  downloadTreeSitter(location, treeSitterPath)
  logger.debug { "🌲 Installed tree-sitter to $treeSitterPath" }
}

private suspend fun FileSystem.downloadTreeSitter(jnaLibraryPath: Path, treeSitterPath: Path) {
  val tempFile = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve("tree-sitter.gz")
  deleteRecursively(tempFile, mustExist = false)
  logger.debug { "🌲 Downloading tree-sitter to $tempFile" }
  val remoteTreeSitterRelease = CommandExecutor.DEFAULT.platform().getTreeSitterUrl()
  logger.debug { "🌲 Downloading tree-sitter from $remoteTreeSitterRelease" }
  download(remoteTreeSitterRelease, tempFile)
  decompressGzip(tempFile, treeSitterPath)
  val remoteTreeSitterLangPack = CommandExecutor.DEFAULT.platform().getTreeSitterLangsPackUrl()
  logger.debug { "🌲 Downloading tree-sitter langs pack from $remoteTreeSitterLangPack" }
  val langsFile = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve("langs.zip")
  download(remoteTreeSitterLangPack, langsFile)
  unzipLangPack(langsFile, jnaLibraryPath)
}

private fun FileSystem.unzipLangPack(source: Path, target: Path) {
  ZipInputStream(
    FileInputStream(source.toFile())
  ).use { zis ->
    var entry = zis.nextEntry
    while (entry != null) {
      if (entry.isDirectory) {
        val newFile = target.resolve(entry.name)
        createDirectories(newFile)
      } else {
        // todo hardcode for macos
        val name = "lib${entry.name.substringBeforeLast(".")}.dylib"
        val newFile = target.resolve(name)
        newFile.parent?.let { createDirectories(it) }
        write(newFile) {
          zis.copyTo(this.outputStream())
        }
      }
      zis.closeEntry()
      entry = zis.nextEntry
    }
  }
}

@Throws(IOException::class)
private fun decompressGzip(source: Path, target: Path) {
  GZIPInputStream(
    FileInputStream(source.toFile())
  ).use { gis ->
    FileOutputStream(target.toFile()).use { fos ->

      // copy GZIPInputStream to FileOutputStream
      val buffer = ByteArray(1024)
      var len: Int
      while (gis.read(buffer).also { len = it } > 0) {
        fos.write(buffer, 0, len)
      }
    }
  }
}

private fun Platform.getTreeSitterLangsPackUrl(): String {
  val downloadPlatform =
    when (this) {
      is Platform.WINDOWS -> "windows"
      is Platform.LINUX -> "linux"
      else -> {
        logger.warn { "🌲 Unsupported platform $this, defaulting to `linux`" }
        "linux"
      }
    }
  return "https://github.com/anasrar/nvim-treesitter-parser-bin/releases/download/$downloadPlatform/all.zip"
}


/**
 * TODO add support for other platforms
 * https://github.com/tree-sitter/tree-sitter/releases/download/v0.20.8/
 *
 * tree-sitter-linux-arm.gz
 * tree-sitter-linux-arm64.gz
 * tree-sitter-linux-x64.gz
 * tree-sitter-linux-x86.gz
 * tree-sitter-macos-arm64.gz
 * tree-sitter-macos-x64.gz
 * tree-sitter-windows-x64.gz
 * tree-sitter-windows-x86.gz
 */
private fun Platform.getTreeSitterUrl(version: String = "v0.20.8"): String {
  val downloadUrl = "https://github.com/tree-sitter/tree-sitter/releases/download/$version/"
  val name = when (archName) {
    "x86_64" -> "x64"
    else -> archName
  }
  return when (this) {
    is Platform.LINUX -> downloadUrl + "tree-sitter-linux-$name.gz"
    is Platform.MACOS -> downloadUrl + "tree-sitter-macos-$name.gz"
    is Platform.WINDOWS -> downloadUrl + "tree-sitter-windows-$name.gz"
  }
}

suspend fun treeSitter(treeSitterPath: Path, lang: String, source: String): Tree {
  FileSystem.DEFAULT.installTreeSitter(treeSitterPath)
  val logger = KotlinLogging.logger {}
  val log = Logger()
  log.log =
    object : LogCallback {
      override fun log(payload: Pointer?, type: Int, msg: String) {
        logger.debug { "🌲 $msg" }
      }
    }
  val parser = Parser()
  TreeSitter.INSTANCE.ts_parser_set_logger(parser, log)
  parser.language = AnyLangLibrary.loadLanguage(lang).language()
  return parser.parseString(null, source)
}
