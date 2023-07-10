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
  val allSymbols: List[Symbol] = List(
    TypeRepr.of[A].typeSymbol.declarations,
    TypeRepr.of[A].typeSymbol.caseFields,
    TypeRepr.of[A].typeSymbol.children,
    TypeRepr.of[A].typeSymbol.declaredFields,
    TypeRepr.of[A].typeSymbol.declaredMethods,
    TypeRepr.of[A].typeSymbol.declaredTypes,
    TypeRepr.of[A].typeSymbol.fieldMembers,
    TypeRepr.of[A].typeSymbol.methodMembers,
    TypeRepr.of[A].typeSymbol.paramSymss.flatten,
    TypeRepr.of[A].typeSymbol.primaryConstructor.paramSymss.flatten,
    TypeRepr.of[A].typeSymbol.typeMembers
  ).flatten.distinct

  def isDerived(ter: Term): Boolean =
    TypeRepr.of[jla.Annotation].classSymbol.exists(ter.tpe.derivesFrom)

  def toAnnotations(sym: Symbol): Expr[List[Annotation]] =
    Expr.ofList(sym.annotations.filterNot(isDerived).map(_.asExprOf[Annotation]))

  Expr.ofList(allSymbols.map(sym => Expr.ofTuple(Expr(sym.name) -> toAnnotations(sym))))

}
