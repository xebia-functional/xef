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
- [In Scala](https://github.com/xebia-functional/xef/blob/main/docs/intro/scala.md)

You can also have a look at the examples:

- [In Kotlin](https://github.com/xebia-functional/xef/tree/main/examples/kotlin/src/main/kotlin/com/xebia/functional/xef/auto)
- [In Scala](https://github.com/xebia-functional/xef/tree/main/examples/scala/src/main/scala/com/xebia/functional/xef/scala/auto)

to have a feeling of how using the library looks like.

## Data Transmission Disclosure

While this library is licensed under the Apache License, it's crucial
to inform our users about specific data transmission behaviors associated
with using this software.

This library may transmit source code and potentially user input data to
third-party services as part of its functionality. We understand the paramount
importance of data security and privacy, so we want to be upfront about these mechanisms.

**Developers integrating this library into their applications should be aware
of this behavior and take necessary precautions to ensure that sensitive data
is not inadvertently transmitted.**

We strongly recommend reviewing the third-party services' privacy policies
before using this library, as their data handling practices may not align with
your expectations or requirements.

You acknowledge and agree to these data transmission behaviors by using this
library. Please consider this when planning your data management and privacy
strategies.
