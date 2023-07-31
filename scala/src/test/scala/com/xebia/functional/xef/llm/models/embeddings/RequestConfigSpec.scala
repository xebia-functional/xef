package com.xebia.functional.xef.llm.models.embeddings

import munit.FunSuite

class RequestConfigSpec extends FunSuite:

  test("Should create a RequestConfig through the static apply method") {
    val requestConfig = RequestConfig(EmbeddingModel.TEXT_EMBEDDING_ADA_002, "user")

    assertEquals(requestConfig.getModel, EmbeddingModel.TEXT_EMBEDDING_ADA_002)
  }
