# Technical

This document tracks technical decisions for the Core module,
such that we can document and backtrack our decisions.

There is a trade-off between building a lean and mean _core_ library,
and writing as much as common code in Kotlin Multiplatform as possible.

To achieve this we try to keep the dependencies on external dependencies as small as possible,
but we have a need for a couple _base_ dependencies which we document below.
The breakdown is only in terms of JVM, since that's where we _mostly_ care about this.

## Kotlin's dependency breakdown (JVM - common)

We include the following dependencies in our _core_ module to implement a _common_ layer to interact with LLMs.
The dependency on Kotlin Stdlib is unavoidable, since we use Kotlin as our main language.
We also require KotlinX Coroutines such that we can leverage the `suspend` keyword in our API and expose `Future` to the
Java/Scala API.
Additionally, we also have a need for a HTTP client, and a serialization framework. Here we use Ktor and KotlinX
Serialization respectively,
and Xef relies on the CIO engine for Ktor, which avoids any additional dependencies.

- kotlin-stdlib (1810 Kb = 1598 Kb + 212 Kb)
- kotlinx-coroutines-core (1608 Kb = 1442 Kb + 166 Kb)
- Ktor Client (288Kb)
- KotlinX Serialization (791 Kb)

Total: 4497 Kb
