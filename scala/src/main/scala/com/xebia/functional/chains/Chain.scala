package com.xebia.functional.scala.chains

import cats.effect.kernel.Async
import cats.syntax.all.*
import com.xebia.functional.scala.chains.models.Config
import com.xebia.functional.chains.Chain as KtChain
import com.xebia.functional.scala.kotlin.CoroutineToIO
import com.xebia.functional.AIError

import scala.jdk.CollectionConverters.*

abstract class Chain[F[_] : Async](ktChain: KtChain):
  val call: Map[String, String] => F[Map[String, String]] = inputs =>
    CoroutineToIO.toAsync[F, java.util.Map[String, String]](ktChain.unsafeCall(inputs.asJava, _)).map(_.asScala.toMap)

  val run: Map[String, String] | String => F[Map[String, String]] = inputs =>
    CoroutineToIO.toAsync[F, java.util.Map[String, String]](
      inputs match
        case str: String => ktChain.unsafeRun(str, _)
        case strMap: Map[String, String] => ktChain.unsafeRun(strMap.asJava, _)
    ).map(_.asScala.toMap)
