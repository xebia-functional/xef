package org.example.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class JvmInlineAnnotationTest(private val env: KotlinCoreEnvironment) {

  @Test
  fun `reports missing jvminline annotations`() {
    val code = """
        value class User(val id: String)
        """
    val findings = JvmInlineAnnotation(Config.empty).compileAndLintWithContext(env, code)
    findings shouldHaveSize 1
  }

  @Test
  fun `doesn't report annotated value classes`() {
    val code =
      """
				import kotlin.jvm.JvmInline
        @JvmInline value class User(val id: String)
        """
    val findings = JvmInlineAnnotation(Config.empty).compileAndLintWithContext(env, code)
    findings shouldHaveSize 0
  }
}
