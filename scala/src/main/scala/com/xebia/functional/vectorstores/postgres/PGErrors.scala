package com.xebia.functional.vectorstores.postgres

import scala.util.control.NoStackTrace

object PGErrors:
  class DatabaseSetupError(reason: String) extends Throwable with NoStackTrace:
    override def getMessage: String = s"Error while setting up the database: $reason"

  class CollectionNotFoundError(collectionName: String) extends Throwable with NoStackTrace:
    override def getMessage: String = s"Collection '$collectionName' not found"

  class EmbeddingNotGeneratedError(text: String) extends Throwable with NoStackTrace:
    override def getMessage(): String = s"Embedding for text: '$text', has not been properly generated"
