package com.xebia.functional.xef.evaluator

import arrow.core.Either
import arrow.core.NonEmptyList
import com.xebia.functional.xef.evaluator.models.SuiteSpec
import com.xebia.functional.xef.evaluator.models.errors.ValidationError
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object EitherOps {
  fun Either<NonEmptyList<ValidationError>, SuiteSpec>.toJsonFile(
    output: String = ".",
    filename: String = "data.json"
  ) {
    File("$output/$filename").writeText(toJson())
  }

  fun Either<NonEmptyList<ValidationError>, SuiteSpec>.toJson() =
    fold(
      { errs -> Json.encodeToString(errs.map { it.message }.toList()) },
      { suiteSpec -> Json.encodeToString(suiteSpec) }
    )
}
