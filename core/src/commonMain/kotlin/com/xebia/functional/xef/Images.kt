package com.xebia.functional.xef

import ai.xef.openai.OpenAIModel
import ai.xef.openai.StandardModel
import com.xebia.functional.openai.apis.ImagesApi
import com.xebia.functional.openai.apis.UploadFile
import com.xebia.functional.openai.infrastructure.HttpResponse
import com.xebia.functional.openai.models.CreateImageEditRequestModel
import com.xebia.functional.openai.models.CreateImageRequest
import com.xebia.functional.openai.models.CreateImageRequestModel
import com.xebia.functional.openai.models.ImagesResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

data class Images(val api: ImagesApi) {

  sealed class Image {
    data class Url(
      val url: String,
      val revisedPrompt: String,
    ) : Image()

    data class B64Json(val content: String, val revisedPrompt: String) : Image()
  }

  suspend fun image(
    prompt: String,
    amount: Int = 1,
    quality: CreateImageRequest.Quality = CreateImageRequest.Quality.standard,
    model: OpenAIModel<CreateImageRequestModel> = StandardModel(CreateImageRequestModel.dall_e_2),
    responseFormat: CreateImageRequest.ResponseFormat = CreateImageRequest.ResponseFormat.url,
    propertySize: CreateImageRequest.PropertySize = CreateImageRequest.PropertySize._1024x1024,
    style: CreateImageRequest.Style = CreateImageRequest.Style.vivid,
    user: String = "user"
  ): Flow<Image> = flow {
    val response =
      api.createImage(
        CreateImageRequest(
          prompt = prompt,
          model = model,
          n = amount,
          quality = quality,
          responseFormat = responseFormat,
          propertySize = propertySize,
          style = style,
          user = user
        )
      )
    handleResponse(prompt, response)
  }

  private suspend fun FlowCollector<Image>.handleResponse(
    prompt: String,
    response: HttpResponse<ImagesResponse>
  ): Unit {
    response.body().data.forEach {
      val url = it.url
      val revisedPrompt = it.revisedPrompt ?: prompt
      val b64Json = it.b64Json
      if (url != null) {
        emit(Image.Url(url, revisedPrompt))
      } else if (b64Json != null) {
        emit(Image.B64Json(b64Json, revisedPrompt))
      }
    }
  }

  private suspend fun Image.asInputProvider(): UploadFile =
    when (this) {
      is Image.Url -> {
        api.client.prepareGet(url).execute {
          val source = it.body<ByteReadPacket>()
          UploadFile(filename = "image", bodyBuilder = { source.copyTo(this) })
        }
      }
      is Image.B64Json -> {
        UploadFile(filename = "image", bodyBuilder = { writeText(content) })
      }
    }

  suspend fun edit(
    prompt: String,
    image: Image,
    mask: Image? = null,
    amount: Int = 1,
    responseFormat: ImagesApi.ResponseFormatCreateImageEdit =
      ImagesApi.ResponseFormatCreateImageEdit.url,
    propertySize: ImagesApi.PropertySizeCreateImageEdit =
      ImagesApi.PropertySizeCreateImageEdit._1024x1024,
    user: String = "user"
  ): Flow<Image> = flow {
    val response =
      api.createImageEdit(
        image = image.asInputProvider(),
        prompt = prompt,
        mask = mask?.asInputProvider(),
        n = amount,
        size = propertySize,
        responseFormat = responseFormat,
        user = user
      )
    handleResponse(prompt, response)
  }

  suspend fun variant(
    image: Image,
    model: CreateImageEditRequestModel = CreateImageEditRequestModel.dall_e_2,
    amount: Int = 1,
    responseFormat: ImagesApi.ResponseFormatCreateImageVariation =
      ImagesApi.ResponseFormatCreateImageVariation.url,
    propertySize: ImagesApi.PropertySizeCreateImageVariation =
      ImagesApi.PropertySizeCreateImageVariation._1024x1024,
    user: String = "user"
  ): Flow<Image> = flow {
    val response =
      api.createImageVariation(
        image = image.asInputProvider(),
        model = model,
        n = amount,
        size = propertySize,
        responseFormat = responseFormat,
        user = user
      )
    handleResponse("", response)
  }
}
