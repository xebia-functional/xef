package org.example.detekt

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.descriptors.runtime.structure.desc
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.isValueClassType
import org.jetbrains.kotlin.types.KotlinType

/** Fixes occurrences of inline data class when enabled to allow for jvm language interop. */
class PublicDataClassConstructorWithValueParameters(config: Config) : Rule(config) {

  override val issue: Issue =
    Issue(
      javaClass.sim,
      Severity.Defect,
      "This rule reports public data classes that are incompatible with other jvm languages.",
      Debt.FIVE_MINS
    )

  /** * Reports inline value classes not marked @JvmInline as a lint warning. */
  override fun visitClass(ktClass: KtClass) {
    try {
      if (
        ktClass.isData() &&
          ktClass.hasPrimaryConstructor() &&
          ktClass.primaryConstructor?.valueParameters?.any {
            val ktpe = bindingContext.get(BindingContext.TYPE, it.typeReference)

            (ktpe as KotlinType).unwrap().isValueClassType()
          } == true
      ) {
        var hasJvmStaticConstructor = false
        val hasCompanionObjects = ktClass.companionObjects.isNotEmpty()
        if (!hasCompanionObjects) {
          hasJvmStaticConstructor = false
        } else {
          ktClass.companionObjects.forEach { ktObjectDeclaration ->
            if (!hasJvmStaticConstructor) {
              ktObjectDeclaration.acceptChildren(
                object : KtVisitorVoid() {
                  override fun visitKtElement(element: KtElement) {
                    try {
                      element.acceptChildren(this)
                    } catch (_: UnsupportedOperationException) {}
                  }

                  override fun visitNamedFunction(function: KtNamedFunction) {
                    if (!hasJvmStaticConstructor) {
                      val klassParametersAsString =
                        ktClass.primaryConstructor?.valueParameterList?.parameters?.fold("") {
                          acc,
                          parameter ->
                          if (acc == "") {
                            acc + parameter.name + ":" + parameter.typeReference?.text
                          } else {
                            acc + ", " + parameter.name + ":" + parameter.typeReference?.text
                          }
                        }
                      val functionParametersAsStrings =
                        function.valueParameterList?.parameters?.fold("") { acc, parameter ->
                          if (acc == "") {
                            acc + parameter.name + ":" + parameter.typeReference?.text
                          } else {
                            acc + ", " + parameter.name + ":" + parameter.typeReference?.text
                          }
                        }
                      val functionHasJvmStatic =
                        function.annotationEntries.any { it.shortName?.asString() == "JvmStatic" }
                      val functionHasAllButLastDefaultConstructorParameter =
                        klassParametersAsString == functionParametersAsStrings
                      hasJvmStaticConstructor =
                        functionHasJvmStatic && functionHasAllButLastDefaultConstructorParameter
                    }
                  }
                }
              )
            }
          }
        }
        if (!hasJvmStaticConstructor) {
          report(
            CorrectableCodeSmell(
              issue,
              Entity.from(ktClass),
              "Kotlin data classes containing parameters that are value classes must have a JVM static factory method to be compatible with other jvm languages.",
              emptyList(),
              listOf(Entity.from(ktClass)),
              true
            )
          )
        }
      }
    } catch (_: Exception) {}
  }
}
