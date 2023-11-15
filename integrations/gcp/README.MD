# Building Kotlin Multiplatform network layer

Building a Kotlin Multiplatform network libraries we need several different pieces,
this document will cover all pieces and how to setup and build from start-to-finish.

## Setting up Gradle

### Kotlin Multiplatform

The most simple solution would be to copy an existing http module, and modify the Gradle setup as desired.
But let's cover the different important pieces. First we need to set up the Gradle plugins to configure both
_Kotlin Multiplatform_, and _KotlinX Serialization_ for content negotiation.

```groovy
id("org.jetbrains.kotlin.multiplatform") version "1.9.10"
```

In the Xef project we've already defined these dependencies in the [Version Catalog](),
so you get a typed DSL inside Gradle to set this up with automatic versioning.

```groovy
id(libs.plugins.kotlin.multiplatform.get().pluginId)
```

The Kotlin Multiplatform plugin sets up Gradle so that we can rely on the `kotlin` DSL,
and set up the targets for the desired platforms.

For Xef we set up following targets:

```groovy
kotlin {
  jvm()
  js(IR) {
    browser()
    nodejs()
  }

  linuxX64()
  macosX64()
  macosArm64()
  mingwX64()
}
```

This creates different _sourceSets_, which are linked to certain targets. This is done in an hierarchy.
`commonMain` is at the top of the hierarchy,
all code defined here is available from platform specific sourceSets and platforms.
When building multiplatform http clients, we're going to define everything in `commonMain` so
we're going to ignore the other ones for now.

We can now write code in `src/commonMain/kotlin` as you normally would for Java in `src/main/java`,
but remember you only have access to _common_ Kotlin code. So now JDK specific packages are available.

### KotlinX Serialization

So now we've configured Kotlin, we should set up KotlinX Serialization, and it's compiler plugin.
This is needed so we can send `JSON`, and different formats over the network.

```groovy
id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
```

In the Xef project we've already defined these dependencies in the [Version Catalog](),
so you get a typed DSL inside Gradle to set this up with automatic versioning.

```groovy
id(libs.plugins.kotlinx.serialization.get().pluginId)
```

The `serialization` plugin sets up KotlinX Serialization such that we get access to `@Serializable`,
and the Kotlin Serialization Compiler plugin is correctly configured.

### Dependencies

Finally, we need to set up some dependencies for our project.
We'll start with setting up some _common_ dependencies, we do so again within `kotlin` DSL.

First let's add a dependency on `xef-core`, such that we can implement the `Chat`, `ChatWithFunction`, etc. interfaces
based on our integration.

```groovy
kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(projects.xefCore)
      }
    }
  }
}
```

Finally, we also need to set up Ktor. For most HTTP integration we need 3 _common_ dependencies.

```groovy
implementation("io.ktor:ktor-client-core:2.3.2")
implementation("io.ktor:ktor-client-content-negotiation:2.3.2")
implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.2")
```

There are bundles in the Xef project, so we can more easily depend on them in a typed way.

```groovy
kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(projects.xefCore)
        implementation(libs.bundles.ktor.client)
      }
    }
  }
}
```

Now we'll have access to all the Ktor classes we need to build our Http integration, which we'll cover below.
This however only sets up the _common_ APIs, and no actual http engines for the configured platforms.
So if we'd try to run any code, we'll end up with a runtime error since there is no actual HTTP engine to run the code
with: `"Failed to find HttpClientEngineContainer. Consider adding [HttpClientEngine] implementation in dependencies."`.

So we need to configure the engines, we're immediately going to reference the version catalog DSL here.
Almost all targets are relying on the `CIO` engine, which is the _Coroutines_ engine except for Javascript and Windows.
There are plenty of other options we can choose, more
info [here](https://ktor.io/docs/http-client-engines.html#minimal-version).

```groovy
kotlin {
  sourceSets {
    ...
    val jvmMain by getting {
      dependencies {
        implementation(libs.logback)
        api(libs.ktor.client.cio)
      }
    }
    val jsMain by getting {
      dependencies {
        api(libs.ktor.client.js)
      }
    }
    val linuxX64Main by getting {
      dependencies {
        api(libs.ktor.client.cio)
      }
    }
    val macosX64Main by getting {
      dependencies {
        api(libs.ktor.client.cio)
      }
    }
    val macosArm64Main by getting {
      dependencies {
        api(libs.ktor.client.cio)
      }
    }
    val mingwX64Main by getting {
      dependencies {
        api(libs.ktor.client.winhttp)
      }
    }
  }
}
```

Now that we've completely finished setting up Gradle for our Kotlin Multiplatform library,
all that is left is writing the actual code!

## Writing your first Multiplatform Http library

### Configuring Ktor's HttpClient

Writing a http layer using Ktor is quite simple, everything works through `HttpClient`.
The first thing we need to do is configure the `HttpClient` to work with _Content Negotiation_,
such that we can send `JSON` or other formats over the network.

```kotlin
HttpClient {
  install(ContentNegotiation) {
    json()
  }
}
```

We can pass a custom KotlinX Serialization `Json` instance, such that we can configure it to our needs.
We typically want to use `encodeDefaults = false` such that default `null` arguments are not included in the `JSON`,
and `isLenient = true` and `ignoreUnknownKeys = true` such that serialization is more _lenient_ and robust against
changes.

```kotlin
Json {
  encodeDefaults = false
  isLenient = true
  ignoreUnknownKeys = true
}
```

### Ktor's Httpclient AutoCloseable

Like most `HttpClient`'s the Ktor client holds a lot of internal state,
such as `CoroutineScope`, downstream engines such as `Netty` or `CIO` and schedulers.
So the `HttpClient` implements a `Closeable` interface, on which we need to call `close` when we're finished using
the `HttpClient`, and requests that are still in progress will at that point also be cancelled
with `CancellationException`.

The simplest way is to use the `use` DSL, as follows:

```kotlin
HttpClient().use { client ->
  // use client
}
```

but we typically want to rely on this `HttpClient` from within a `class`, and thus we want to wrap it and propagate
the `Closeable` requirement. Most convenient we do this by implementing `AutoCloseable` from Kotlin Standard Library in
our own class, and delegating to the `HttpClient#close` method.

```kotlin
class GcpClient(/* constructor parameters */) : AutoCloseable {
  private val http: HttpClient = HttpClient {
    // configure client
  }

  override fun close() {
    http.close()
  }
}
```

Now that we've correctly wrapped our `HttpClient`, we're finally read to start making our calls.

## Ktor http calls

The `HttpClient` expose the typical HTTP methods we expect as methods,
together with a builder which we can use to configure the `HttpRequest`.

```kotlin
http.post(
  "https://$apiEndpoint/v1/projects/$projectId/locations/us-central1/publishers/google/models/$modelId:predict"
) {
  header("Authorization", "Bearer $token")
  contentType(ContentType.Application.Json)
  setBody(body)
}
```

Here we call the `post` http method, and pass the URL we want to send the request to.
We configure the `Authorization` header, more on that later, and the `contentType` and we set a _body_.

The `body` in our case is of _content type_ `Json`, which will be automatically serialized from our KotlinX
Serialization compatible class.

So for the example of GCP we want to send following `JSON`:

```json
{
  "instances": [
    {
      "messages": [
        {
          "author": "user",
          "content": "How can I reverse a list in python?"
        }
      ]
    }
  ],
  "parameters": {
    "temperature": 0.3,
    "maxOutputTokens": 200,
    "topK": 40,
    "topP": 0.8
  }
}
```

Which translates to following Kotlin hierarchy:

```kotlin
@Serializable
private data class Prompt(val instances: List<Instance>, val parameters: Parameters? = null)

@Serializable
private data class Instance(
  val context: String? = null,
  val examples: List<Example>? = null,
  val messages: List<Message>,
)

@Serializable
data class Example(val input: String, val output: String)

@Serializable
private data class Message(val author: String, val content: String)

@Serializable
private class Parameters(
  val temperature: Double? = null,
  val maxOutputTokens: Int? = null,
  val topK: Int? = null,
  val topP: Double? = null
)
```

With this defined, we can simply construct our data and pass it to `setBody`,
and we'll receive an `HttpResponse` as result of the _suspend_ `post` call.

```kotlin
val body =
  Prompt(
    listOf(Instance(messages = listOf(Message(author = "user", content = prompt)))),
    Parameters(temperature, maxOutputTokens, topK, topP)
  )

val response: HttpResponse =
  http.post(...) {
  ...
  setBody(body)
}
```

All that's left now is to _deserialize_ the `HttpResponse`, and we do this in the same way as above.
We define a set of Kotlin classes that correspond to the structure of the `JSON`,
and we can deserialize it by calling `body<MyClass>()` on the `HttpResponse`.
Before doing so we typically want to check the `HttpStatusCode`.

```kotlin
if (response.status.isSuccess()) response.body<MyClass>()
else throw GcpClientException(response.status, response.bodyAsText())
```

### Authorization

In the example above we've showed simple authorization using a token,
but in some cases we need more advanced authorization support.

Ktor has a wide support of different authorization support out of the box,
including OAuth2 with refresh tokens.

There is a detailed guide on the [Ktor website](https://ktor.io/docs/auth.html),
this is provided through the `ktor-client-auth` module.

### Retry & Timeouts

Often we also want to have some retry mechanism, and timeout support for our networking.
This can be easily configured on the `HttpClient`, and customised for every individual request as needed.

```kotlin
HttpClient {
  install(HttpTimeout) {
    requestTimeoutMillis = 60_000 // 60 seconds
    connectTimeoutMillis = 60_000 // 60 seconds
    socketTimeoutMillis = 300_000 // 5 minutes
  }
  install(HttpRequestRetry) { // optional, default settings
    retryOnExceptionOrServerErrors(3)
    exponentialDelay()
  }
}
```

## Implementing Core's Chat interface

TODO
