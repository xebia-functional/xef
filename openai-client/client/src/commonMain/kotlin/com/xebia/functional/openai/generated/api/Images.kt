/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.api

import com.xebia.functional.openai.Config
import com.xebia.functional.openai.UploadFile
import com.xebia.functional.openai.appendGen
import com.xebia.functional.openai.generated.api.Images.*
import com.xebia.functional.openai.generated.model.CreateImageEditRequestModel
import com.xebia.functional.openai.generated.model.CreateImageRequest
import com.xebia.functional.openai.generated.model.ImagesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.path
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json

/**  */
interface Images {

  /**
   * Creates an image given a prompt.
   *
   * @param createImageRequest
   * @param configure optional configuration for the request, allows overriding the default
   *   configuration.
   * @return ImagesResponse
   */
  suspend fun createImage(
    createImageRequest: CreateImageRequest,
    configure: HttpRequestBuilder.() -> Unit = {}
  ): ImagesResponse

  /** enum for parameter size */
  @Serializable
  enum class PropertySizeCreateImageEdit(val value: kotlin.String) {

    @SerialName(value = "256x256") _256x256("256x256"),
    @SerialName(value = "512x512") _512x512("512x512"),
    @SerialName(value = "1024x1024") _1024x1024("1024x1024")
  }

  /** enum for parameter responseFormat */
  @Serializable
  enum class ResponseFormatCreateImageEdit(val value: kotlin.String) {

    @SerialName(value = "url") url("url"),
    @SerialName(value = "b64_json") b64_json("b64_json")
  }

  /**
   * Creates an edited or extended image given an original image and a prompt.
   *
   * @param image The image to edit. Must be a valid PNG file, less than 4MB, and square. If mask is
   *   not provided, image must have transparency, which will be used as the mask.
   * @param prompt A text description of the desired image(s). The maximum length is 1000
   *   characters.
   * @param mask An additional image whose fully transparent areas (e.g. where alpha is zero)
   *   indicate where &#x60;image&#x60; should be edited. Must be a valid PNG file, less than 4MB,
   *   and have the same dimensions as &#x60;image&#x60;. (optional)
   * @param model (optional)
   * @param n The number of images to generate. Must be between 1 and 10. (optional, default to 1)
   * @param size The size of the generated images. Must be one of &#x60;256x256&#x60;,
   *   &#x60;512x512&#x60;, or &#x60;1024x1024&#x60;. (optional, default to 1024x1024)
   * @param responseFormat The format in which the generated images are returned. Must be one of
   *   &#x60;url&#x60; or &#x60;b64_json&#x60;. (optional, default to url)
   * @param user A unique identifier representing your end-user, which can help OpenAI to monitor
   *   and detect abuse. [Learn more](/docs/guides/safety-best-practices/end-user-ids). (optional)
   * @param configure optional configuration for the request, allows overriding the default
   *   configuration.
   * @return ImagesResponse
   */
  suspend fun createImageEdit(
    image: UploadFile,
    prompt: kotlin.String,
    mask: UploadFile? = null,
    model: CreateImageEditRequestModel? = null,
    n: kotlin.Int? = 1,
    size: PropertySizeCreateImageEdit? = PropertySizeCreateImageEdit._1024x1024,
    responseFormat: ResponseFormatCreateImageEdit? = ResponseFormatCreateImageEdit.url,
    user: kotlin.String? = null,
    configure: HttpRequestBuilder.() -> Unit = {}
  ): ImagesResponse

  /** enum for parameter responseFormat */
  @Serializable
  enum class ResponseFormatCreateImageVariation(val value: kotlin.String) {

    @SerialName(value = "url") url("url"),
    @SerialName(value = "b64_json") b64_json("b64_json")
  }

  /** enum for parameter size */
  @Serializable
  enum class PropertySizeCreateImageVariation(val value: kotlin.String) {

    @SerialName(value = "256x256") _256x256("256x256"),
    @SerialName(value = "512x512") _512x512("512x512"),
    @SerialName(value = "1024x1024") _1024x1024("1024x1024")
  }

  /**
   * Creates a variation of a given image.
   *
   * @param image The image to use as the basis for the variation(s). Must be a valid PNG file, less
   *   than 4MB, and square.
   * @param model (optional)
   * @param n The number of images to generate. Must be between 1 and 10. For &#x60;dall-e-3&#x60;,
   *   only &#x60;n&#x3D;1&#x60; is supported. (optional, default to 1)
   * @param responseFormat The format in which the generated images are returned. Must be one of
   *   &#x60;url&#x60; or &#x60;b64_json&#x60;. (optional, default to url)
   * @param size The size of the generated images. Must be one of &#x60;256x256&#x60;,
   *   &#x60;512x512&#x60;, or &#x60;1024x1024&#x60;. (optional, default to 1024x1024)
   * @param user A unique identifier representing your end-user, which can help OpenAI to monitor
   *   and detect abuse. [Learn more](/docs/guides/safety-best-practices/end-user-ids). (optional)
   * @param configure optional configuration for the request, allows overriding the default
   *   configuration.
   * @return ImagesResponse
   */
  suspend fun createImageVariation(
    image: UploadFile,
    model: CreateImageEditRequestModel? = null,
    n: kotlin.Int? = 1,
    responseFormat: ResponseFormatCreateImageVariation? = ResponseFormatCreateImageVariation.url,
    size: PropertySizeCreateImageVariation? = PropertySizeCreateImageVariation._1024x1024,
    user: kotlin.String? = null,
    configure: HttpRequestBuilder.() -> Unit = {}
  ): ImagesResponse
}

fun Images(client: HttpClient, config: Config): Images =
  object : Images {
    override suspend fun createImage(
      createImageRequest: CreateImageRequest,
      configure: HttpRequestBuilder.() -> Unit
    ): ImagesResponse =
      client
        .request {
          configure()
          method = HttpMethod.Post
          contentType(ContentType.Application.Json)
          url { path("images/generations") }
          setBody(createImageRequest)
        }
        .body()

    override suspend fun createImageEdit(
      image: UploadFile,
      prompt: kotlin.String,
      mask: UploadFile?,
      model: CreateImageEditRequestModel?,
      n: kotlin.Int?,
      size: PropertySizeCreateImageEdit?,
      responseFormat: ResponseFormatCreateImageEdit?,
      user: kotlin.String?,
      configure: HttpRequestBuilder.() -> Unit
    ): ImagesResponse =
      client
        .request {
          configure()
          method = HttpMethod.Post
          contentType(ContentType.Application.Json)
          url { path("images/edits") }
          setBody(
            formData {
              image?.apply { appendGen("image", image) }
              prompt?.apply { appendGen("prompt", prompt) }
              mask?.apply { appendGen("mask", mask) }
              model?.apply { appendGen("model", model) }
              n?.apply { appendGen("n", n) }
              size?.apply { appendGen("size", size) }
              responseFormat?.apply { appendGen("response_format", responseFormat) }
              user?.apply { appendGen("user", user) }
            }
          )
        }
        .body()

    override suspend fun createImageVariation(
      image: UploadFile,
      model: CreateImageEditRequestModel?,
      n: kotlin.Int?,
      responseFormat: ResponseFormatCreateImageVariation?,
      size: PropertySizeCreateImageVariation?,
      user: kotlin.String?,
      configure: HttpRequestBuilder.() -> Unit
    ): ImagesResponse =
      client
        .request {
          configure()
          method = HttpMethod.Post
          contentType(ContentType.Application.Json)
          url { path("images/variations") }
          setBody(
            formData {
              image?.apply { appendGen("image", image) }
              model?.apply { appendGen("model", model) }
              n?.apply { appendGen("n", n) }
              responseFormat?.apply { appendGen("response_format", responseFormat) }
              size?.apply { appendGen("size", size) }
              user?.apply { appendGen("user", user) }
            }
          )
        }
        .body()
  }
