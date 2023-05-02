package com.xebia.functional.vectorstores.db

import cats.effect.IO

import com.dimafeng.testcontainers.ContainerDef
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.dimafeng.testcontainers.munit.TestContainerForAll
import com.dimafeng.testcontainers.munit.TestContainersSuite
import com.xebia.functional.config.DBConfig
import doobie.Transactor
import doobie.munit.IOChecker
import munit.CatsEffectSuite
import munit.Clue.generate
import org.testcontainers.containers.Network
import org.testcontainers.utility.DockerImageName

trait DatabaseSuite extends CatsEffectSuite with TestContainerForAll with IOChecker:

  override val containerDef: PostgreSQLContainer.Def = PostgreSQLContainer.Def(
    dockerImageName = DockerImageName.parse("ankane/pgvector").asCompatibleSubstituteFor("postgres")
  )

  lazy val transactor = withContainers { container =>
    Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = container.jdbcUrl,
      user = container.username,
      pass = container.password
    )
  }
