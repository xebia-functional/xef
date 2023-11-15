# xef.ai [![Maven Central](https://img.shields.io/maven-central/v/com.xebia/xef-core?color=4caf50&label=latest%20release)](https://central.sonatype.com/artifact/com.xebia/xef-core)

> Bring modern AI everywhere!

xef is the one-stop library to bring the power of modern AI to your application or service,
in the form of LLM (Large Language Models), image generation, and many others.
Our goal is to make the move to this new world as simple as possible for the developer.
xef.ai is packaged in two layers:
1. Core libraries bringing integration with the basic services in an AI application. 
   These libraries expose an _idiomatic_ interface, so there's one per programming language.
   At this moment we support Kotlin.
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

1. `xef-core` is the core library.
2. `xef-openai` is the integration with OpenAI's API.
3. The name of a library we provide integration for, like `xef-lucene`.

You may need to  add that repository explicitly in your build, if you haven't done it before.

```groovy
repositories { mavenCentral() }
```

Then add the libraries in the usual way.

```groovy
// In Gradle Kotlin 
dependencies {
  implementation("com.xebia:xef-core:<version>")
  implementation("com.xebia:xef-openai:<version>")
}
```

We publish all libraries at once under the same version, so
[version catalogs](https://docs.gradle.org/current/userguide/platforms.html#sec:sharing-catalogs)
could be useful.

## 📖 Quick Introduction

In [this](https://github.com/xebia-functional/xef/blob/main/docs/intro.md) small introduction we look at the main features of xef, including the `conversation` function.

## 🚀 Examples

You can also have a look at the [examples](https://github.com/xebia-functional/xef/tree/main/examples/src/main/kotlin/com/xebia/functional/xef/conversation) to have a feeling of how using the library looks like.
