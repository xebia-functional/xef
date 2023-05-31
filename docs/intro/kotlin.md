# Quick introduction to xef.ai (Kotlin version)

After adding the library to your project
(see the [main README](https://github.com/xebia-functional/xef/blob/main/README.md) for instructions),
you get access to the `ai` function, which is your port of entry to the modern AI world.
Inside of it, you can _prompt_ for information, which means posing the question to an LLM
(Large Language Model). The easiest way is to just get the information back as a string.

```kotlin
import com.xebia.functional.xef.auto.*

fun books(topic: String): List<String> = ai {
    promptMessage("Give me a selection of books about $topic")
}.getOrThrow()
```

> **Note**
> By default the `ai` block connects to [OpenAI](https://platform.openai.com/).
> To use their services you should provide the corresponding API key in the `OPENAI_TOKEN`
> environment variable, and have enough credits.

In the example above we _execute_ the `ai` block with `getOrThrow`, that throws an exception
whenever a problem is found (for example, if your API key is not correct). If you want more
control, you can use `getOrElse` (to which you provide a custom handler for errors), or
`toEither` (which returns the result using 
[`Either` from Arrow](https://arrow-kt.io/learn/typed-errors/either-and-ior/)).

In the next examples we'll drop the `getOrThrow`, so we get the result from `ai` directly.
This is of the form `AI<Something>`, where `Something` is the type you eventually obtain
after performing the AI calls. Within an `ai` block you can freely get the result from
those `AI` values; this allows you to break a larger pipeline into small pieces.

## Structure

The output from the `books` function above may be hard to parse back from the
strings we obtain. Fortunately, you can also ask xef.ai to give you back the information
using a _custom type_. The library takes care of instructing the LLM on building such
a structure, and deserialize the result back for you.

```kotlin
import com.xebia.functional.xef.auto.*

@Serializable
data class Book(val title: String, val author: String)

fun books(topic: String): AI<List<Book>> = ai {
    prompt("Give me a selection of books about $topic")
}
```

xef.ai reuses [Kotlin's common serialization](https://kotlinlang.org/docs/serialization.html),
which requires adding the `kotlinx.serialization` plug-in to your build, and mark each
class as `@Serializable`. The LLM is usually able to detect which kind of information should
go on each field based on its name (like `title` and `author` above).

## Prompt templates

The function `books` uses naive string interpolation to make the topic part of the question
to the LLM. As the prompt gets bigger, though, you may want to break it into smaller parts.
The `buildPrompt` function is the tool here: inside of it you can include any string or
smaller prompt by prefixing it with `+`
(this is known as the [builder pattern](https://kotlinlang.org/docs/type-safe-builders.html)).

```kotlin
import com.xebia.functional.xef.auto.*

@Serializable
data class Book(val title: String, val author: String)

fun books(topic: String): AI<List<Book>> = ai {
  val prompt = buildPrompt {
    + "Give me a selection of books about the following topic:"
    + topic
  }
  prompt(prompt)
}
```

In a larger AI application it's common to end up with quite some template for prompts.
Online material like [this course](https://www.deeplearning.ai/short-courses/chatgpt-prompt-engineering-for-developers/)
and [this tutorial](https://learnprompting.org/docs/intro) explain some of the most important patterns,
some of them readily available in xef.ai.

## Context

LLMs have knowledge about a broad variety of topics. But by construction they are not able
to respond to questions about information not available in their training set. However, you
often want to supplement the LLM with more data:
- Transient information referring to the current moment, like the current weather, or
  the trends in the stock market in the past 10 days.
- Non-public information, for example for summarizing a piece of text you're creating
  within you organization.

These additional pieces of information are called the _context_ in xef.ai, and are attached
to every question to the LLM. Although you can add arbitrary strings to the context at any
point, the most common mode of usage is using an _agent_ to consult an external service,
and make its response part of the context. One such agent is `search`, which uses a web
search service to enrich that context.

```kotlin
import com.xebia.functional.xef.auto.*

fun whatToWear(place: String): AI<List<String>> = ai {
    context(search("Weather in $place")) {
        promptMessage("Knowing this forecast, what clothes do you recommend I should wear?")
    }
}
```

> **Note**
> The underlying mechanism of the context is a _vector store_, a data structure which
> saves a set of strings, and is able to find those similar to another given one.
> By default xef.ai uses an _in-memory_ vector store, since it provides maximum
> compatibility across platforms. However, if you foresee your context growing above
> the hundreds of elements, you may consider switching to another alternative, like
> Lucene or PostgreSQL.
> 
> ```kotlin
> import com.xebia.functional.xef.auto.*
> import com.xebia.functional.xef.vectorstores
> 
> fun books(topic: String): AI<List<Book>> = ai {
>   withContextStore(InMemoryLuceneBuilder(LUCENE_PATH)) { /* do stuff */ }
> }
> ```
