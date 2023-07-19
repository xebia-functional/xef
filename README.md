# xef.ai [![Maven Central](https://img.shields.io/maven-central/v/com.xebia/xef-core?color=4caf50&label=latest%20release)](https://central.sonatype.com/artifact/com.xebia/xef-core)

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

<!-- TOC -->
* [⚠️ Data Transmission Disclosure](#-data-transmission-disclosure)
* [🛎️ Getting the Libraries](#-getting-the-libraries)
* [📖 Quick Introduction](#-quick-introduction)
* [🚀 Examples](#-examples)
<!-- TOC -->

## ⚠️ Data Transmission Disclosure

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

## 🛎️ Getting the Libraries

Libraries are published in Maven Central, under the `com.xebia` group.

1. `xef-kotlin` for Kotlin support, `xef-scala` for Scala, `xef-java` for Java.
2. The name of a library we provide integration for, like `xef-lucene`.

<details>
<summary><img src="https://upload.wikimedia.org/wikipedia/commons/3/37/Kotlin_Icon_2021.svg" height="15px" alt="Kotlin logo"> Gradle (Kotlin DSL)</summary>

Libraries are published in Maven Central. You may need to  add that repository explicitly
in your build, if you haven't done it before.

```kotlin
repositories {
    mavenCentral()
}
```

Then add the library in the usual way.

```kotlin
// In Gradle Kotlin 
dependencies {
    implementation("com.xebia:xef-kotlin:<version>")
}
```

We publish all libraries at once under the same version, so
[version catalogs](https://docs.gradle.org/current/userguide/platforms.html#sec:sharing-catalogs)
could be useful.

</details>

<details>
<summary><img src="https://www.scala-lang.org/resources/img/frontpage/scala-spiral.png" height="15px" alt="Scala logo"> SBT</summary>

```sbt
libraryDependencies += "com.xebia" %% "xef-scala" % "<version>"
```

> **Warning**
> `xef-scala` is currently only available for Scala 3, and depends on project [Loom](https://openjdk.org/projects/loom/),
> so you will need at least Java 19 to use the library.

</details>

<details>
<summary><img src="https://en.wikipedia.org/wiki/Apache_Maven#/media/File:Apache_Maven_logo.svg" height="15px" alt="Maven logo"> Maven</summary>

Libraries are published in Maven Central. You may need to  add that repository explicitly
in your build, if you haven't done it before.

```xml
<dependency>
   <groupId>com.xebia</groupId>
   <artifactId>xef-java</artifactId>
   <version>x.x.x</version>
   <type>pom</type>
   <scope>runtime</scope>
</dependency>
```

</details>

## 📖 Quick Introduction

In this small introduction we look at the main features of xef, including the `ai` function.

- [<img src="https://upload.wikimedia.org/wikipedia/commons/3/37/Kotlin_Icon_2021.svg" height="15px" alt="Kotlin logo"> Kotlin version](https://github.com/xebia-functional/xef/blob/main/docs/intro/kotlin.md)
- [<img src="https://www.scala-lang.org/resources/img/frontpage/scala-spiral.png" height="15px" alt="Scala logo"> Scala version](https://github.com/xebia-functional/xef/blob/main/docs/intro/scala.md)
- [<img src="https://en.wikipedia.org/wiki/Java_(programming_language)#/media/File:Java_programming_language_logo.svg" height="15px" alt="Java logo"> Java version](https://github.com/xebia-functional/xef/blob/main/docs/intro/java.md)

## 🚀 Examples

You can also have a look at the examples to have a feeling of how using the library looks like.

- [<img src="https://upload.wikimedia.org/wikipedia/commons/3/37/Kotlin_Icon_2021.svg" height="15px" alt="Kotlin logo"> Examples in Kotlin](https://github.com/xebia-functional/xef/tree/main/examples/kotlin/src/main/kotlin/com/xebia/functional/xef/auto)
- [<img src="https://www.scala-lang.org/resources/img/frontpage/scala-spiral.png" height="15px" alt="Scala logo"> Examples in Scala](https://github.com/xebia-functional/xef/tree/main/examples/scala/src/main/scala/com/xebia/functional/xef/scala/auto)
- [<img src="https://en.wikipedia.org/wiki/Java_(programming_language)#/media/File:Java_programming_language_logo.svg" height="15px" alt="Java logo"> Examples in Java](https://github.com/xebia-functional/xef/tree/main/examples/java/src/main/java/com/xebia/functional/xef/java/auto)
