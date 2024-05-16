package conf

sealed interface ValidationError {
  data object AwsAccessKeyIdNotProvided : ValidationError

  data object AwsSecretAccessKeyNotProvided : ValidationError
}
