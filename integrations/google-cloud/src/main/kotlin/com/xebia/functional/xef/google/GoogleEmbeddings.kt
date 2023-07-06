package com.xebia.functional.xef.google

import com.google.cloud.aiplatform.v1.*
import com.google.protobuf.Value
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig
import com.xebia.functional.xef.llm.models.usage.Usage
import com.xebia.functional.xef.llm.Embeddings as EmbeddingsModel

class GoogleEmbeddings(
  private val project: String,
  private val location: String,
  private val model: String
) : EmbeddingsModel, Embeddings {

  private val predictionService  = PredictionServiceClient.create()

  private val modelGardenServiceClient: ModelGardenServiceClient = ModelGardenServiceClient.create()

  override val name: String = model

  override fun close() {
    predictionService.close()
    modelGardenServiceClient.close()
  }

  // projects/xefdemo/locations/us-central1/endpoints/textembedding-gecko
// projects/xefdemo/locations/us-central1/publishers/google/models/textembedding-gecko:predict


  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    val instances = request.input.map { Value.newBuilder().setStringValue(it).build() }
    val endpointName = EndpointName.of(project, location, model)

    val predictions: PredictResponse = predictionService.predict(
      endpointName,
      instances,
      Value.getDefaultInstance()
    )

    val embeddings = predictions.predictionsList.map {
      it.structValue.fieldsMap.values.map { it.numberValue }
    }

    return EmbeddingResult(
      data = embeddings.mapIndexed { n, em -> Embedding("embedding", em.map { it.toFloat() }, n) },
      usage = Usage.ZERO
    )
  }

  override suspend fun embedDocuments(
    texts: List<String>,
    chunkSize: Int?,
    requestConfig: RequestConfig
  ): List<com.xebia.functional.xef.embeddings.Embedding> {
    val instances = texts.map { Value.newBuilder().setStringValue(it).build() }

    val publisherModel = modelGardenServiceClient.getPublisherModel(model)

    val predictions: PredictResponse = predictionService.predict(
      EndpointName.of(project, location, publisherModel.name),
      instances,
      Value.getDefaultInstance()
    )

    val embeddings = predictions.predictionsList.map {
      it.structValue.fieldsMap.values.map { it.numberValue }
    }

    return embeddings.mapIndexed { n, em ->
      com.xebia.functional.xef.embeddings.Embedding(em.map { it.toFloat() })
    }
  }

  override suspend fun embedQuery(
    text: String,
    requestConfig: RequestConfig
  ): List<com.xebia.functional.xef.embeddings.Embedding> =
    embedDocuments(listOf(text), null, requestConfig)

  companion object {

    @JvmField
    val DEFAULT_MODEL = "textembedding-gecko"

    @JvmStatic
    operator fun invoke(project: String, location: String, model: String) = GoogleEmbeddings(project, location, model)
  }
}
