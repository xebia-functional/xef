package com.xebia.functional.xef.assistants.tools

import arrow.fx.coroutines.parMap
import com.xebia.functional.openai.apis.FilesApi
import com.xebia.functional.openai.models.AssistantFileObject
import com.xebia.functional.xef.assistants.model.Datasets
import com.xebia.functional.xef.assistants.model.PackageSearchResponse
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.llm.assistants.AssistantThread
import com.xebia.functional.xef.llm.assistants.MessageWithFiles
import com.xebia.functional.xef.llm.assistants.Tool
import com.xebia.functional.xef.llm.fromEnvironment
import io.ktor.client.request.forms.*
import io.ktor.utils.io.streams.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.net.URL

@Serializable
data class Urls(val urls: List<String>)

@Serializable
data class Files(val files: List<File>)

@Serializable
data class File(val name: String)

@Serializable
@Description("Download files from the datasets on data.gov and uploads them to an ongoing thread")
data class DownloadFilesTool(
  @Description("The urls of the files to download")
  val urls: Urls,
) : Tool<Files> {

  override suspend fun invoke(thread: AssistantThread): Files {
    val filesApi = fromEnvironment(::FilesApi)
    val files = urls.urls.parMap { url ->
      URL(url).openStream().use { stream ->
        filesApi.createFile(
          file = InputProvider(null) {
            stream.asInput()
          },
          purpose = FilesApi.PurposeCreateFile.assistants
        )
      }
    }
    val uploadedFiles = files.mapNotNull {
      try {
        val createdFile = it.body()
        println("created file: ${createdFile.filename}")
        createdFile
      } catch (e: Exception) {
        null
      }
    }
    return Files(uploadedFiles.map { File(it.filename) })
  }

  private fun java.io.File.copyInputStreamToFile(inputStream: InputStream) {
    this.outputStream().use { fileOut ->
      inputStream.copyTo(fileOut)
    }
  }
}
