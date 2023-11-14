## Getting the libraries

Libraries are published in Maven Central. You may need to add that repository explicitly
in your build, if you haven't done it before. Then add the library in the usual way.

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.xebia:xef-core:<version>")
    implementation("com.xebia:xef-openai:<version>")
}
```

We publish all libraries at once under the same version, so
[version catalogs](https://docs.gradle.org/current/userguide/platforms.html#sec:sharing-catalogs)
could be useful.

By default, the `OpenAI.conversation` block connects to [OpenAI](https://platform.openai.com/).
To use their services you should provide the corresponding API key in the `OPENAI_TOKEN`
environment variable, and have enough credits.

```shell
env OPENAI_TOKEN=<your-token> <gradle-command>
```

> **Caution / Important**: <br />
>This library may transmit source code and potentially user input data to third-party services as part of its functionality.
>Developers integrating this library into their applications should be aware of this behavior and take necessary precautions to ensure that sensitive data is not inadvertently transmitted.
>Read our [_Data Transmission Disclosure_](https://github.com/xebia-functional/xef#%EF%B8%8F-data-transmission-disclosure) for further information.

## Your first prompt

After adding the library to your project
you get access to the `conversation` function, which is your port of entry to the modern AI world.
Inside of it, you can _prompt_ for information, which means posing the question to an LLM
(Large Language Model). The easiest way is to just get the information back as a string.

```kotlin
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.prompt

suspend fun books(topic: String): String = OpenAI.conversation {
  prompt("Give me a selection of books about $topic")
}
```

## Structure

The output from the `books` function above may be hard to parse back from the
strings we obtain. Fortunately, you can also ask xef.ai to give you back the information
using a _custom type_. The library takes care of instructing the LLM on building such
a structure, and deserialize the result back for you.

```kotlin
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable
data class Books(val books: List<Book>)

@Serializable
data class Book(val title: String, val author: String)

suspend fun books(topic: String): Books = OpenAI.conversation {
  prompt("Give me a selection of books about $topic")
}
```

xef.ai reuses [Kotlin's common serialization](https://kotlinlang.org/docs/serialization.html),
which requires adding the `kotlinx.serialization` plug-in to your build, and mark each
class as `@Serializable`. The LLM is usually able to detect which kind of information should
go on each field based on its name (like `title` and `author` above).
For those cases where the LLM is not able to infer the type, you can use the `@Description` annotation:

## @Description annotations

```kotlin
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.prompt
import kotlinx.serialization.Serializable

@Serializable
@Description("A list of books")
data class Books(
  @Description("The list of books")
  val books: List<Book>
)

@Serializable
@Description("A book")
data class Book(
  @Description("The title of the book")
  val title: String, 
  @Description("The author of the book")
  val author: String,
  @Description("A 50 word summary of the book")
  val summary: String
)

suspend fun books(topic: String): Books = OpenAI.conversation {
  prompt("Give me a selection of books about $topic")
}
```

All the types and properties annotated with `@Description` will be used to build the
json schema `description` fields used for the LLM to reply with the right format and data
in order to deserialize the result back.

## Prompts

The function `books` uses naive string interpolation to make the topic part of the question
to the LLM. As the prompt gets bigger, though, you may want to break it into smaller parts.
The `buildPrompt` function is the tool here: inside of it you can include messages and other prompts
which get built before the chat completions endpoint.
(this is known as the [builder pattern](https://kotlinlang.org/docs/type-safe-builders.html)).

```kotlin
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.llm.openai.prompt
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.*
import kotlinx.serialization.Serializable

@Serializable
data class Book(val title: String, val author: String)

@Serializable
data class Books(val books: List<Book>)

suspend fun Conversation.books(topic: String): Books {
  val myPrompt = Prompt {
    +system("You are an assistant in charge of providing a selection of books about topics provided")
    +assistant("I will provide relevant suggestions of books and follow the instructions closely.")
    +user("Give me a selection of books about $topic")
  }
  return prompt(myPrompt)
}
```

This style of prompting is more effective than simple strings messages as it describes a scene of how the LLM
should behave and reply. We use different roles for each message constructed with the `Prompt` builder.

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
import com.xebia.functional.xef.reasoning.serpapi.Search

@Serializable
data class MealPlan(
  val name: String,
  val recipes: List<Recipe>
)

suspend fun mealPlan() {
  OpenAI.conversation {
    val search = Search(OpenAI.FromEnvironment.DEFAULT_CHAT, this)
    addContext(search("gall bladder stones meals"))
    prompt("Meal plan for the week for a person with gall bladder stones that includes 5 recipes.")
  }
}

```

:::note Better vector stores

The underlying mechanism of the context is a _vector store_, a data structure which
saves a set of strings, and is able to find those similar to another given one.
By default xef.ai uses an _in-memory_ vector store, since it provides maximum
compatibility across platforms. However, if you foresee your context growing above
the hundreds of elements, you may consider switching to another alternative, like
Lucene or PostgreSQL also supported by xef.
