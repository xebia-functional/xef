package com.xebia.functional.scala.chains.models

import cats.ApplicativeThrow

import com.xebia.functional.scala.chains.models.InvalidCombineDocumentsChainError

enum CombineDocumentsChainType(val name: String):
  case Stuff extends CombineDocumentsChainType("stuff")

object CombineDocumentsChainType:
  def fromString[F[_]: ApplicativeThrow](chainType: String): F[CombineDocumentsChainType] =
    chainType match
      case "stuff" => ApplicativeThrow[F].pure(Stuff)
      case _ => ApplicativeThrow[F].raiseError(InvalidCombineDocumentsChainError(chainType))
