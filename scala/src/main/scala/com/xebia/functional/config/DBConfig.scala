package com.xebia.functional.scala.config

import cats.syntax.all._

import ciris.*

final case class DBConfig(
    url: String,
    user: String,
    password: String,
    driverClassName: String,
    awaitConnectionThreadPoolSize: Int,
    vectorSize: Int
)

object DBConfig:
  def configValue[F[_]]: ConfigValue[F, DBConfig] =
    (
      env("DB_URL").as[String].default(""),
      env("DB_USER").as[String].default(""),
      env("DB_PASSWORD").as[String].default(""),
      env("DB_DRIVER_CLASS_NAME").as[String].default("org.postgresql.Driver"),
      env("DB_THREAD_POOL_SIZE").as[Int].default(8),
      env("DB_VECTOR_SIZE").as[Int].default(1536)
    ).parMapN(DBConfig.apply)
