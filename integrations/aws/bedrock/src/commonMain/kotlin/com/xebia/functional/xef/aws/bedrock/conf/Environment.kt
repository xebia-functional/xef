package com.xebia.functional.xef.aws.bedrock.conf

import arrow.core.NonEmptyList
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import conf.ValidationError
import java.lang.System.getenv

@JvmInline
value class Secret(val value: String) {
  override fun toString(): String {
    return value.replaceRange(0, value.length - 3, "*")
  }
}

fun String.secret(): Secret = Secret(this)

data class Environment(val aws: Aws = Aws()) {
  data class Aws(
    val credentials: Credentials = Credentials(),
    val regionName: String = getenv("AWS_REGION_NAME") ?: "us-east-1"
  ) {
    data class Credentials(
      val accessKeyId: String = getenv("AWS_ACCESS_KEY_ID"),
      val secretAccessKey: Secret = getenv("AWS_SECRET_ACCESS_KEY").secret()
    )
  }
}

fun loadEnvironment(): Environment {
  val environment = Environment()
  return either<NonEmptyList<ValidationError>, Environment> {
      zipOrAccumulate(
        {
          ensure(environment.aws.credentials.accessKeyId.isNotBlank()) {
            raise(ValidationError.AwsAccessKeyIdNotProvided)
          }
        },
        {
          ensure(environment.aws.credentials.secretAccessKey.value.isNotBlank()) {
            raise(ValidationError.AwsSecretAccessKeyNotProvided)
          }
        },
        { _, _ -> environment }
      )
    }
    .getOrElse { throw RuntimeException(it.joinToString(transform = ValidationError::toString)) }
}
