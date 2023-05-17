## xef.ai

`xef` is the one-stop library to bring the power of modern AI to your application or service,
in the form of LLM (Large Language Models), image generation, and many others.
Our goal is to make the move to this new world as simple as possible for the developer.

---

## Getting started

```kotlin
repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation("com.47deg.xef:xef-core:0.0.1-SNAPSHOT")
}
```

---

```kotlin
import com.xebia.functional.xef.auto.*

@Serializable
data class UserPreferences(val favoriteGenre: String, val dislikedGenre: String, val favoriteDirector: String)

@Serializable
data class MovieRecommendation(val title: String, val genre: String, val description: String)

fun recommendMovie(userPreferences: UserPreferences): MovieRecommendation = 
  ai {
    context(userPreferences) {
      prompt("""|Recommend a movie based on these preferences: 
                |Favorite genre : ${userPreferences.favoriteGenre}
                |Disliked genre : ${userPreferences.dislikedGenre}
                |Favorite director : ${userPreferences.favoriteDirector}
                |""".trimMargin())
    }
  }.getOrThrow()

```

---

## Custom types

```kotlin
import com.xebia.functional.xef.auto.*

@Serializable
data class Book(val title: String, val author: String)

fun books(topic: String): List<Book> = ai {
  prompt("Give me a selection of books about $topic")
}.getOrThrow()
```

---

## Prompt Templates

```kotlin
import com.xebia.functional.xef.auto.*

@Serializable
data class Book(val title: String, val author: String)

fun books(topic: String): List<Book> = ai {
    val template = PromptTemplate(
        "Give me a selection of books about the topic given between triple backticks: ```{topic}```",
        listOf("topic")
    )
    prompt(template, mapOf("topic" to topic))
}.getOrThrow()
```

---

## Context scopes

```kotlin
import com.xebia.functional.xef.auto.*

fun whatToWear(place: String): List<String> = ai {
    context(search("Weather in $place")) {
        promptMessage("Knowing this forecast, what clothes do you recommend I should wear?")
    }
}
```

--- 

## Context store scopes

```kotlin
import com.xebia.functional.xef.auto.*
import com.xebia.functional.xef.vectorstores

fun books(topic: String) = ai {
  withContextStore(InMemoryLuceneBuilder(LUCENE_PATH)) { 
    /* do stuff with lucene store */ 
  }
}.getOrThrow()
```

---

## AI is programs as values

As any other value you can pass it around

```kotlin
import com.xebia.functional.xef.auto.*
import com.xebia.functional.xef.vectorstores

@Serializable
data class AIResponse(val value: String)

val program: AI<AIResponse> = ai {
  prompt("A poem about programming languages")
}

val result: Either<AIError, AIResponse> = program.toEither()
```

---

## AI is an extensible DSL

You can create your own functions for the AI scope operations

```kotlin
import com.xebia.functional.xef.auto.*

@Serializable
data class Original(val text: String)

@Serializable
data class Summarized(val summary: Text)

fun AIScope.summarize(original: Original): Summarized =
  prompt("summarize this text: ${original.text}")

suspend fun main() = ai {
  val poem: Original = prompt("A poem about programming languages")
  val summarized = summarize(poem)
  println(summarized)
}.getOrThrow()
```

---
