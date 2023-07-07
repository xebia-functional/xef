package com.xebia.functional.xef.scala.auto

import scala.annotation.Annotation
import java.lang.{annotation => jla}

/**
 * Wraps scala annotations in a java annotation extension so that scala annotations can be used during serialization.
 *
 * @param a
 *   scala annotation subtype.
 */
final class WrappedAnnotation[A <: Annotation](val a: A) extends jla.Annotation:
  override def annotationType(): Class[? <: java.lang.annotation.Annotation] =
    getClass()
  override def toString(): String = s"""|WrappedAnnotation(
                                        |  ${a.toString()}
                                        |)""".stripMargin
