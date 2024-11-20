package org.example.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.CorrectableCodeSmell
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtClass

/** Fixes occurrences of inline data class when enabled to allow for jvm language interop. */
class JvmInlineAnnotation(config: Config) : Rule(config) {

  override val issue: Issue =
    Issue(
      javaClass.simpleName,
      Severity.Defect,
      "This rule reports an public inline value class as incompatible with other jvm languages.",
      Debt.FIVE_MINS
    )

  /**
   * * Reports inline value classes not marked @JvmInline as a lint warning, and autofixes the
   *   declaration to include @JvmInline.
   */
  override fun visitClass(klass: KtClass) {

    if (
      klass.isValue() &&
        klass.annotationEntries.filter { e -> e.shortName?.asString() == "JvmInline" }.size < 1
    ) {
      report(
        CorrectableCodeSmell(
          issue,
          Entity.from(klass),
          "Kotlin inline value classes are not compatible with other jvm languages.",
          emptyList(),
          listOf(Entity.from(klass)),
          false
        )
      )
    }
  }
}
