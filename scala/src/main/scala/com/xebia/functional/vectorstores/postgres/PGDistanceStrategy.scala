package com.xebia.functional.scala.vectorstores.postgres

enum PGDistanceStrategy(val strategy: String):
  case Euclidean extends PGDistanceStrategy("<->")
  case InnerProduct extends PGDistanceStrategy("<#>")
  case CosineDistance extends PGDistanceStrategy("<=>")
