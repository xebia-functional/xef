package com.xebia.functional.xef.scala.auto

import scala.quoted.*
import scala.annotation.Annotation
import java.lang.{annotation => jla}

/**
 * Gets all the member and type annotations for type A
 *
 * @return
 *   A list of key value pairs, with the member name as the key and the scala Annotations for the member as the value, filtering out any non-scala
 *   annotations (they cause an InstantiationException when created via ``Expr`).
 */
inline def getAnnotationsMacro[A]: List[(String, List[Annotation])] = ${
  getAnnotationsImpl[A]
}

/**
 * Lists all the representable member annotations
 *
 * @return
 *   An expression containing the member name and member annotations without java annotations for the given type A.
 */
def getAnnotationsImpl[A: Type](using
    Quotes
): Expr[List[(String, List[Annotation])]] = {
  import quotes.reflect.*
  Expr.ofList(
    (TypeRepr
      .of[A]
      .typeSymbol
      .declarations ++
      TypeRepr
        .of[A]
        .typeSymbol
        .caseFields ++
      TypeRepr
        .of[A]
        .typeSymbol
        .children ++
      TypeRepr
        .of[A]
        .typeSymbol
        .declaredFields ++
      TypeRepr
        .of[A]
        .typeSymbol
        .declaredMethods ++
      TypeRepr
        .of[A]
        .typeSymbol
        .declaredTypes ++
      TypeRepr
        .of[A]
        .typeSymbol
        .fieldMembers ++
      TypeRepr
        .of[A]
        .typeSymbol
        .methodMembers ++
      TypeRepr
        .of[A]
        .typeSymbol
        .paramSymss
        .flatten ++
      TypeRepr
        .of[A]
        .typeSymbol
        .primaryConstructor
        .paramSymss
        .flatten ++
      TypeRepr
        .of[A]
        .typeSymbol
        .typeMembers).distinct.map(term => (term, term.annotations)).map { (term, ats) =>
      Expr.ofTuple(
        (
          Expr(term.name),
          Expr.ofList(
            ats
              .filterNot { ter =>
                TypeRepr
                  .of[jla.Annotation]
                  .classSymbol
                  .map(ter.tpe.derivesFrom)
                  .getOrElse(false)
              }
              .map[Expr[Annotation]] { at =>
                at.asExprOf[Annotation]
              }
          )
        )
      )
    }
  )
}
