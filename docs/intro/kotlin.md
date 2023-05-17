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

## Structure

The output from the `books` function above may be hard to parse back from the
strings we obtain. Fortunately, you can also ask xef.ai to give you back the information
using a _custom type_. The library takes care of instructing the LLM on building such
a structure, and deserialize the result back for you.

```kotlin
import com.xebia.functional.xef.auto.*

@Serializable
data class Book(val title: String, val author: String)

fun books(topic: String): List<Book> = ai {
    prompt("Give me a selection of books about $topic")
}.getOrThrow()
```

xef.ai reuses [Kotlin's common serialization](https://kotlinlang.org/docs/serialization.html),
which require adding the `kotlinx.serialization` plug-in to your build, and mark each
class as `@Serializable`. The LLM is usually able to detect which kind of information should
go on each field based on its name (like `title` and `author` above).

## Prompt templates

The function `books` uses naive string interpolation to make the topic part of the question
to the LLM. This is not the best practice, because the way the topic is phrased may confuse
the LLM. It's recommended
(for example, [here](https://www.deeplearning.ai/short-courses/chatgpt-prompt-engineering-for-developers/)
and [here](https://learnprompting.org/docs/intro)) to use a _delimiter_ instead, and create a 
template to avoid string manipulation and promote reuse.

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

As you can see above, a _template_ describes a message pattern alongside a set of _variables_;
in our case only `topic`. The call to prompt accepts that template, and a map of values for
each of the variables.

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

fun whatToWear(place: String): List<String> = ai {
    context(search("Weather in $place")) {
        promptMessage("Knowing this forecast, what clothes do you recommend I should wear?")
    }
}.getOrThrow()
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
> fun books(topic: String) = ai {
>   withContextStore(InMemoryLuceneBuilder(LUCENE_PATH)) { /* do stuff */ }
> }.getOrThrow()
> ```
