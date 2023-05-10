package com.xebia.functional.scala.vectorstores.db

import cats.effect.Async
import cats.effect.kernel.Resource

import com.xebia.functional.scala.config.DBConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object DoobieTransactor:
  def make[F[_]: Async](conf: DBConfig): Resource[F, HikariTransactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](conf.awaitConnectionThreadPoolSize)
      xa <- HikariTransactor.newHikariTransactor[F](
        driverClassName = conf.driverClassName,
        url = conf.url,
        user = conf.user,
        pass = conf.password,
        connectEC = ce
      )
    } yield xa
