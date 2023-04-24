package com.xebia.functional.tools

import cats.MonadThrow
import eu.timepit.refined.types.string.NonEmptyString

abstract class BaseTools[F[_]: MonadThrow](
    name: NonEmptyString,
    description: NonEmptyString,
    // argsSchema: Option[???]
    returnDirect: Boolean = false,
    verbose: Boolean = false
    // callbackManager: BaseCallbackManager
)
