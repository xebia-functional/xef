package org.example.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class PublicDataClassConstructorWithValueParametersTest(
  private val env: KotlinCoreEnvironment
) {
  @Test
  fun `reports data classes that have value class parameters without companion static constructor`() {
    val code =
      """
      import kotlin.jvm.JvmInline
      @JvmInline value class  Bar(val value: String)
      data class Foo(val bar: Bar)
      """
    val findings =
      PublicDataClassConstructorWithValueParameters(Config.empty)
        .compileAndLintWithContext(env, code)
    findings.forEach { println(it.message) }
    findings shouldHaveSize 1
  }

  @Test
  fun `doesn't report data classes with value parameters with companion static constructor`() {
    val code =
      """
      import kotlin.jvm.JvmInline
      import kotlin.jvm.JvmStatic
      @JvmInline value class  Bar(val value: String)
      data class Foo(val bar: Bar) {
        companion object {
          @JvmStatic
          fun apply(bar: Bar): Foo {
            return Foo(bar)
          }
        }
      }"""
    val findings = JvmInlineAnnotation(Config.empty).compileAndLintWithContext(env, code)
    findings shouldHaveSize 0
  }

  @Test
  fun `reports data classes with value parameters with companion objects without static factory methods`() {
    val code =
      """
      import kotlin.jvm.JvmInline
      @JvmInline value class  Bar(val value: String)
      data class Foo(val bar: Bar){
        companion object {
          val test: String = "test"
          fun notAFactoryMethod(): String = test
        }
      }"""
    val findings =
      PublicDataClassConstructorWithValueParameters(Config.empty)
        .compileAndLintWithContext(env, code)
    findings.forEach { println(it.message) }
    findings shouldHaveSize 1
  }

  @Test
  fun `reports data classes with value parameters with companion objects with static factory methods that are missing arguments of the targeted class, even if marked JvmStatic`() {
    val code =
      """
      import kotlin.jvm.JvmInline
      import kotlin.jvm.JvmStatic
      @JvmInline value class  Bar(val value: String)
      data class Foo(val bar: Bar){ 
        companion object {
          val test: Bar = Bar("test")
          @JvmStatic fun applyMissingArguments():Foo  = Foo(test)
        }
      }"""
    val findings =
      PublicDataClassConstructorWithValueParameters(Config.empty)
        .compileAndLintWithContext(env, code)
    findings.forEach { println(it.message) }
    findings shouldHaveSize 1
  }

  @Test
  fun `does not report data classes with value parameters that have static factory methods in their companion objects that have all the arguments of the targeted class that are marked JvmStatic`() {
    val code =
      """
      import kotlin.jvm.JvmStatic
      import kotlin.jvm.JvmInline
      @JvmInline value class Bar(val value: String)
      data class Foo(val bar: Bar) {
        companion object { 
          val test: String = "test"
          @JvmStatic fun apply(bar: Bar):Foo  = Foo(bar)
        }
      }"""
    val findings =
      PublicDataClassConstructorWithValueParameters(Config.empty)
        .compileAndLintWithContext(env, code)
    findings shouldHaveSize 0
  }
}
