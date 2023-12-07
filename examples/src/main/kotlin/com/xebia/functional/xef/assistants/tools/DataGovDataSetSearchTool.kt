package com.xebia.functional.xef.assistants.tools

import com.xebia.functional.xef.assistants.model.Datasets
import com.xebia.functional.xef.assistants.model.PackageSearchResponse
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.llm.assistants.AssistantThread
import com.xebia.functional.xef.llm.assistants.Tool
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

@Serializable
@Description("Search for datasets on data.gov")
data class DataGovDataSetSearchTool(
  @Description("Required. The query to search for")
  val query: String,
) : Tool<Datasets> {

  override suspend fun invoke(thread: AssistantThread): Datasets {
    return try {
      val sanitizedQuery = query.encodeURLParameter()
      val text = URL("https://catalog.data.gov/api/3/action/package_search?q=$sanitizedQuery").readText()
      val response = json.decodeFromString<PackageSearchResponse>(text)
      if (response.success) {
        response.datasets()
      } else {
        throw Exception(response.help)
      }
    } catch (e: Exception) {
      Datasets.empty
    }
  }



  private fun PackageSearchResponse.datasets(): Datasets {
    val datasets = result.results.map {
      val files = it.resources.map { it.datasetFile() }
      dataset(it, files)
    }
    return Datasets(
      count = result.count,
      results = datasets
    )
  }

  private fun dataset(
    it: PackageSearchResponse.Result.Result,
    files: List<Datasets.DatasetFile>
  ) = Datasets.Dataset(
    name = it.name,
    notes = it.notes,
    files = files
  )

  private fun PackageSearchResponse.Result.Result.Resource.datasetFile() =
    Datasets.DatasetFile(
      name = name,
      url = url ?: "",
      format = format,
      mimetype = mimetype,
      size = size,
      created = created,
      last_modified = last_modified
    )

  companion object {
    private val json = Json { ignoreUnknownKeys = true }
  }
}
