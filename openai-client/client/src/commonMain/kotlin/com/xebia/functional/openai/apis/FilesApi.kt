/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.apis

import com.xebia.functional.openai.infrastructure.*
import com.xebia.functional.openai.models.DeleteFileResponse
import com.xebia.functional.openai.models.ListFilesResponse
import com.xebia.functional.openai.models.OpenAIFile
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.forms.formData
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json

open class FilesApi : ApiClient {

  constructor(
    baseUrl: String = ApiClient.BASE_URL,
    httpClientEngine: HttpClientEngine? = null,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
    jsonSerializer: Json = ApiClient.JSON_DEFAULT
  ) : super(
    baseUrl = baseUrl,
    httpClientEngine = httpClientEngine,
    httpClientConfig = httpClientConfig,
    jsonBlock = jsonSerializer
  )

  constructor(
    baseUrl: String,
    httpClient: HttpClient
  ) : super(baseUrl = baseUrl, httpClient = httpClient)

  /** enum for parameter purpose */
  @Serializable
  enum class PurposeCreateFile(val value: kotlin.String) {

    @SerialName(value = "fine-tune") fine_tune("fine-tune"),
    @SerialName(value = "assistants") assistants("assistants")
  }

  /**
   * Upload a file that can be used across various endpoints. The size of all the files uploaded by
   * one organization can be up to 100 GB. The size of individual files can be a maximum of 512 MB
   * or 2 million tokens for Assistants. See the [Assistants Tools guide](/docs/assistants/tools) to
   * learn more about the types of files supported. The Fine-tuning API only supports
   * &#x60;.jsonl&#x60; files. Please [contact us](https://help.openai.com/) if you need to increase
   * these storage limits.
   *
   * @param file The File object (not file name) to be uploaded.
   * @param purpose The intended purpose of the uploaded file. Use \\\&quot;fine-tune\\\&quot; for
   *   [Fine-tuning](/docs/api-reference/fine-tuning) and \\\&quot;assistants\\\&quot; for
   *   [Assistants](/docs/api-reference/assistants) and [Messages](/docs/api-reference/messages).
   *   This allows us to validate the format of the uploaded file is correct for fine-tuning.
   * @return OpenAIFile
   */
  @Suppress("UNCHECKED_CAST")
  open suspend fun createFile(
    file: com.xebia.functional.openai.apis.UploadFile,
    purpose: PurposeCreateFile
  ): HttpResponse<OpenAIFile> {

    val localVariableAuthNames = listOf<String>("ApiKeyAuth")

    val localVariableBody = formData {
      file?.apply { appendGen("file", file) }
      purpose?.apply { appendGen("purpose", purpose) }
    }

    val localVariableQuery = mutableMapOf<String, List<String>>()
    val localVariableHeaders = mutableMapOf<String, String>()

    val localVariableConfig =
      RequestConfig<kotlin.Any?>(
        RequestMethod.POST,
        "/files",
        query = localVariableQuery,
        headers = localVariableHeaders,
        requiresAuthentication = true,
      )

    return multipartFormRequest(localVariableConfig, localVariableBody, localVariableAuthNames)
      .wrap()
  }

  /**
   * Delete a file.
   *
   * @param fileId The ID of the file to use for this request.
   * @return DeleteFileResponse
   */
  @Suppress("UNCHECKED_CAST")
  open suspend fun deleteFile(fileId: kotlin.String): HttpResponse<DeleteFileResponse> {

    val localVariableAuthNames = listOf<String>("ApiKeyAuth")

    val localVariableBody = io.ktor.client.utils.EmptyContent

    val localVariableQuery = mutableMapOf<String, List<String>>()
    val localVariableHeaders = mutableMapOf<String, String>()

    val localVariableConfig =
      RequestConfig<kotlin.Any?>(
        RequestMethod.DELETE,
        "/files/{file_id}".replace("{" + "file_id" + "}", "$fileId"),
        query = localVariableQuery,
        headers = localVariableHeaders,
        requiresAuthentication = true,
      )

    return request(localVariableConfig, localVariableBody, localVariableAuthNames).wrap()
  }

  /**
   * Returns the contents of the specified file.
   *
   * @param fileId The ID of the file to use for this request.
   * @return kotlin.String
   */
  @Suppress("UNCHECKED_CAST")
  open suspend fun downloadFile(fileId: kotlin.String): HttpResponse<kotlin.String> {

    val localVariableAuthNames = listOf<String>("ApiKeyAuth")

    val localVariableBody = io.ktor.client.utils.EmptyContent

    val localVariableQuery = mutableMapOf<String, List<String>>()
    val localVariableHeaders = mutableMapOf<String, String>()

    val localVariableConfig =
      RequestConfig<kotlin.Any?>(
        RequestMethod.GET,
        "/files/{file_id}/content".replace("{" + "file_id" + "}", "$fileId"),
        query = localVariableQuery,
        headers = localVariableHeaders,
        requiresAuthentication = true,
      )

    return request(localVariableConfig, localVariableBody, localVariableAuthNames).wrap()
  }

  /**
   * Returns a list of files that belong to the user&#39;s organization.
   *
   * @param purpose Only return files with the given purpose. (optional)
   * @return ListFilesResponse
   */
  @Suppress("UNCHECKED_CAST")
  open suspend fun listFiles(purpose: kotlin.String? = null): HttpResponse<ListFilesResponse> {

    val localVariableAuthNames = listOf<String>("ApiKeyAuth")

    val localVariableBody = io.ktor.client.utils.EmptyContent

    val localVariableQuery = mutableMapOf<String, List<String>>()
    purpose?.apply { localVariableQuery["purpose"] = listOf("$purpose") }
    val localVariableHeaders = mutableMapOf<String, String>()

    val localVariableConfig =
      RequestConfig<kotlin.Any?>(
        RequestMethod.GET,
        "/files",
        query = localVariableQuery,
        headers = localVariableHeaders,
        requiresAuthentication = true,
      )

    return request(localVariableConfig, localVariableBody, localVariableAuthNames).wrap()
  }

  /**
   * Returns information about a specific file.
   *
   * @param fileId The ID of the file to use for this request.
   * @return OpenAIFile
   */
  @Suppress("UNCHECKED_CAST")
  open suspend fun retrieveFile(fileId: kotlin.String): HttpResponse<OpenAIFile> {

    val localVariableAuthNames = listOf<String>("ApiKeyAuth")

    val localVariableBody = io.ktor.client.utils.EmptyContent

    val localVariableQuery = mutableMapOf<String, List<String>>()
    val localVariableHeaders = mutableMapOf<String, String>()

    val localVariableConfig =
      RequestConfig<kotlin.Any?>(
        RequestMethod.GET,
        "/files/{file_id}".replace("{" + "file_id" + "}", "$fileId"),
        query = localVariableQuery,
        headers = localVariableHeaders,
        requiresAuthentication = true,
      )

    return request(localVariableConfig, localVariableBody, localVariableAuthNames).wrap()
  }
}
