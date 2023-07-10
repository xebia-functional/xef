package com.xebia.functional.xef.scala.auto

import java.lang.{annotation => jla}
import scala.reflect.ClassTag

/**
 * Retrieves the scala and java annotations for a type `A` given a `ClassTag[A]`
 *
 * Uses macros to fetch the scala annotation values and java reflection to fetch the java annotation values.
 *
 * Avoids the nested Aux pattern and InstantiationException caused by mixing scala and java annotations when using shapeless deriving to fetch the
 * annotations.
 *
 * @param klassTag
 *   Provides a means for accessing the runtime class of the type
 * @return
 *   The merged map of member name keys as Strings and a list of scala annotations wrapped in the WrappedAnnotation class and regular java
 *   Annotations.
 */
inline def getAnnotations[A](using
    klassTag: ClassTag[A]
): Map[String, List[jla.Annotation]] =
  /**
   * Avoids the need to bring in scala cats as a dependency -- merges two maps with iterable values under the same key
   *
   * @param a
   *   One of two maps to merge
   * @param b
   *   One of two maps to merge
   * @return
   *   The merged map
   */
  def combineIterables[K, V](
      a: Map[K, Iterable[V]],
      b: Map[K, Iterable[V]]
  ): Map[K, Iterable[V]] =
    a ++ b.map { case (k, v) => k -> (v ++ a.getOrElse(k, Iterable.empty)) }

  val klass = klassTag.runtimeClass
  val originalAnnotations = getAnnotationsMacro[A]
  val wrappedScalaAnnotations = originalAnnotations.map { p =>
    (p._1, p._2.map[jla.Annotation](WrappedAnnotation(_)))
  }

  val classAnnotations = List((klass.getName(), klass.getAnnotations().toList))

  val constructorAnnotations = {
    val constructors = klass.getConstructors()
    if (constructors.length == 0) List.empty 
    else 
      constructors(0).getParameters().toList.map { param =>
        (param.getName(), param.getAnnotations().toList)
      }
  }
  val fieldsAnnotations = klass
    .getDeclaredFields()
    .toList
    .map { f =>
      (f.getName(), f.getAnnotations().toList)
    }

  combineIterables(
    combineIterables(
      combineIterables(
        wrappedScalaAnnotations.toMap,
        constructorAnnotations.toMap
      ),
      fieldsAnnotations.toMap
    ),
    classAnnotations.toMap
  ).map(p => (p._1, p._2.toList))
