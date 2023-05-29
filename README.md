# xef.ai

> Bring modern AI everywhere!

xef is the one-stop library to bring the power of modern AI to your application or service,
in the form of LLM (Large Language Models), image generation, and many others.
Our goal is to make the move to this new world as simple as possible for the developer.
xef.ai is packaged in two layers:
1. Core libraries bringing integration with the basic services in an AI application. 
   These libraries expose an _idiomatic_ interface, so there's one per programming language.
   At this moment we support Kotlin and Scala.
2. Integrations with other libraries which complement the core mission of xef.ai.

xef.ai draws inspiration from libraries like [LangChain](https://docs.langchain.com/docs/)
and community projects like [Hugging Face](https://huggingface.co/).

## Getting the libraries

At this moment libraries are published in Sonatype's Snapshot repository. You need to add
that repository to your Maven or Gradle build:

```kotlin
repositories {
    mavenCentral()
    // other repositories
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}
```

Then add `com.xebia:xef-<name>:0.0.1-alpha.<latest-alpha-version>` as a dependency, where `<name>` refers
to the subcomponent you need:

1. `xef-core` for Kotlin support, `xef-scala` for Scala.
2. The name of a library we provide integration for, like `xef-lucene`.

We publish all libraries at once under the same version, so
[version catalogs](https://docs.gradle.org/current/userguide/platforms.html#sec:sharing-catalogs)
could be useful if you use Gradle.

## Quick introduction

- [In Kotlin](https://github.com/xebia-functional/xef/blob/main/docs/intro/kotlin.md)
- In Scala (work in progress)

You can also have a look at our
[examples](https://github.com/xebia-functional/xef/tree/main/examples/kotlin/src/main/kotlin/com/xebia/functional/xef/auto)
to have a feeling of how using the library looks like.
