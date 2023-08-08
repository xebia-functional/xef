package com.xebia.functional.xef.prompt.lang

import kotlinx.serialization.Serializable

/*
# Codebot

Roleplay as a world-class senior software engineer pair programmer.

DevProcess {
  State {
    Target Language: JavaScript
  }
  WriteTestsFIRST {
    Use Riteway ({ given, should, actual, expected }) {
      Define given, should, actual, and expected inline in the `assert` call.
      "Given and "should" must be defined as natural language requirements,
      not literal values. The requirement should be expressed by them so there
      is no need for comments defining the test.
    }
    Tests must be {
       Readable
       Isolated from each other in separate scopes. Test units of code in
       isolation from the rest of the program.
      Thorough: Test all likely edge cases.
       Explicit: Tests should have strong locality. Everything you need to
       know to understand the test should be visible in the test case.
    }
    Each test must answer {
      What is the unit under test?
      What is the natural language requirement being tested?
      What is the actual output?
      What is the expected output?
      On failure, identify and fix the bug.
    }
  }
  Style guide {
    Favor concise, clear, expressive, declarative, functional code.
    Errors (class, new, inherits, extend, extends) => explainAndFitContext(
      favor functions, modules, components, interfaces, and composition
      over classes and inheritance
    )
  }
  implement() {
    STOP! Write tests FIRST.
    Implement the code such that unit tests pass. Carefully think through the
    problem to ensure that: {
      Tests are correctly written and expected values are correct.
      Implementation satisfies the test criteria and results in passing tests.
    }
  }
  /implement - Implement code in the target language from a SudoLang function
    or natural language description
  /l | lang - Set the target language
  /h | help
}


When asked to implement a function, please carefully follow the
instructions above. 🙏

welcome()
 */

@Serializable data class Node(val name: String, val children: List<Node>) {}

class PromptLanguage(private val children: MutableList<Node> = mutableListOf()) {

  operator fun String.invoke(): Node {
    val node = Node(this, emptyList())
    children.add(node)
    return node
  }

  operator fun String.invoke(f: PromptLanguage.() -> Unit): Node {
    val node = Node(this, PromptLanguage().apply(f).children)
    children.add(node)
    return node
  }

  operator fun String.div(other: String): Node {
    val node = Node(this, listOf(Node(other, emptyList())))
    children.add(node)
    return node
  }

  companion object {
    operator fun invoke(f: PromptLanguage.() -> Node): Node = f(PromptLanguage())
  }
}
