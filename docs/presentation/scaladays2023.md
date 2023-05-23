## xef.ai

`xef` is a library to bring the power of modern AI to your application or service,
in the form of LLM (Large Language Models), image generation, and many others.
Our goal is to make the move to this new world as simple as possible for the developer.

-- Animation notes: This goes with the slide where we show the logo.

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

# Multiple language targets

-- Currently supported languages: Scala, Kotlin

Logos of java, scala, rust, go, swift

---

# First AI program

```kotlin
import com.xebia.functional.xef.auto.*

suspend fun main() {
    val response: Either<AIError, List<String>> = ai { //step 1
        promptMessage("Hello, what's your name?") // step 2
    }.toEither() // step 3
                   // step 4 replaces `getOrThrow` for `toEither()` and `val response: List<String>` for `val response: Either<AIError, List<String>>`
    println(response)
}
```

---

# AIErrors

- No Response
- Combined
- Json Parsing
- OpenAI
- Hugging Face
- Prompt Invalid Inputs

---

# Model Driven Design

```kotlin

// step 1
@Serializable
data class Movie(val title: String, val genre: String, val description: String)

suspend fun main() {
  val response: List<Movie> = ai { //step 2
    prompt("List 5 action movies I should watch")
  }.getOrThrow() // end of step 3
  println(response) 
}
// step 4 shows an animation of the AI turning data into json that gets added to the model
```

---

## Prompt Templates

```kotlin
import com.xebia.functional.xef.auto.*

@Serializable
data class Book(val title: String, val author: String)

fun books(topic: String): List<Book> = ai {
    val template = PromptTemplate( // step 1
        "Give me a selection of books about the topic given between triple backticks: ```{topic}```",
        listOf("topic") // step 2
    )
    prompt(template, mapOf("topic" to topic)) // step 3
}.getOrThrow()
```

---

## Context scopes

```kotlin
import com.xebia.functional.xef.auto.*

fun whatToWear(place: String): List<String> = ai {
    context(search("Weather in $place")) { //step 1
        promptMessage("Knowing this forecast, what clothes do you recommend I should wear?") // step 2
    }
}
```

--- 

## Context scopes

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

## Integrations

```kotlin
import com.xebia.functional.xef.pdf.*

// step 1 a PDF to chat with
const val pdfUrl = 
  "https://people.cs.ksu.edu/~schmidt/705a/Scala/Programming-in-Scala.pdf"

suspend fun main() = ai {
  contextScope(pdf(url = pdfUrl)) { //step 2 - add the PDF to the context scope
    while (true) { // step 3 - chats in a loop
      print("Enter your question: ")
      val line = readlnOrNull() ?: break // step 4 - input : "What is this book about?"
      val response: AIResponse = prompt(line) // step 5 - prompt the AI with context
      println("${response.answer}\n---\n${response.source}\n---\n") 
      // step 6 - "Scala is a general-purpose programming language that supports both object-oriented and functional programming. Invented by Martin Odersky and his colleagues at EPFL, Scala has been released for public use in 2004. Scala smoothly integrates the features of object-oriented and functional languages."
    }
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

//step 1
@Serializable
data class Original(val text: String)

@Serializable
data class Summarized(val summary: Text)


//step 2
fun AIScope.summarize(original: Original): Summarized =
  prompt("summarize this text: ${original.text}")

//step 3
suspend fun main() = ai {
  val poem: Original = prompt("A poem about programming languages")
  val summarized = summarize(poem)
  println(summarized)
}.getOrThrow()
```

---
