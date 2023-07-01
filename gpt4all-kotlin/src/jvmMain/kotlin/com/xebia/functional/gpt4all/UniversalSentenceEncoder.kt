package com.xebia.functional.gpt4all

import ai.djl.Application
import ai.djl.MalformedModelException
import ai.djl.ModelException
import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDArrays
import ai.djl.ndarray.NDList
import ai.djl.repository.zoo.Criteria
import ai.djl.repository.zoo.ModelNotFoundException
import ai.djl.training.util.ProgressBar
import ai.djl.translate.NoBatchifyTranslator
import ai.djl.translate.TranslateException
import ai.djl.translate.TranslatorContext
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.stream.Collectors


/**
 * An example of inference using an universal sentence encoder model from TensorFlow Hub.
 *
 *
 * Refer to [Universal Sentence
 * Encoder](https://tfhub.dev/google/universal-sentence-encoder/4) on TensorFlow Hub for more information.
 */
object UniversalSentenceEncoder {

  @Throws(
    MalformedModelException::class,
    ModelNotFoundException::class,
    IOException::class,
    TranslateException::class
  )
  @JvmStatic
  fun embeddings(inputs: List<String>): Array<FloatArray> {
    val modelUrl = "https://storage.googleapis.com/tfhub-modules/google/universal-sentence-encoder/4.tar.gz"
    val criteria = Criteria.builder()
      .optApplication(Application.NLP.TEXT_EMBEDDING)
      .setTypes(
        Array<String>::class.java,
        Array<FloatArray>::class.java
      )
      .optModelUrls(modelUrl)
      .optTranslator(MyTranslator)
      .optEngine("TensorFlow")
      .optProgress(ProgressBar())
      .build()
    criteria.loadModel().use { model ->
      model.newPredictor().use { predictor ->
        return predictor.predict(
          inputs.toTypedArray()
        )
      }
    }
  }
}

object MyTranslator: NoBatchifyTranslator<Array<String>, Array<FloatArray>> {
  override fun processInput(ctx: TranslatorContext, inputs: Array<String>): NDList {
    // manually stack for faster batch inference
    val manager = ctx.ndManager
    val inputsList = NDList(
      Arrays.stream(inputs)
        .map { data: String? ->
          manager.create(
            data
          )
        }
        .collect(Collectors.toList()))
    return NDList(NDArrays.stack(inputsList))
  }

  override fun processOutput(ctx: TranslatorContext, list: NDList): Array<FloatArray> {
    val result = NDList()
    val numOutputs = list.singletonOrThrow().shape[0]
    for (i in 0 until numOutputs) {
      result.add(list.singletonOrThrow()[i])
    }
    return result.stream().map { obj: NDArray -> obj.toFloatArray() }
      .toArray { arrayOf(FloatArray(it)) }
  }
}
