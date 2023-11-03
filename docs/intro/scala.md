# Quick introduction to xef.ai (Scala version)

After adding the library to your project (see the 
[main README](https://github.com/xebia-functional/xef/blob/main/README.md) for instructions),
you get access to the `conversation` function, which is your port of entry to the modern AI world.
Inside of it, you can _prompt_ for information, which means posing the question to an LLM
(Large Language Model). The easiest way is to just get the information back as a string or list of strings.

```scala 3
import com.xebia.functional.xef.scala.conversation.*

def books(topic: String): Unit = conversation:
  val topBook: String = promptMessage(s"Give me the top-selling book about $topic")
  println(topBook)
  val selectedBooks: List[String] = promptMessages(s"Give me a selection of books about $topic")
  println(selectedBooks.mkString("\n"))
```

## Additional setup

If the code above fails, you may need to perform some additional setup.

### OpenAI

By default, the `conversation` block connects to [OpenAI](https://platform.openai.com/).
To use their services you should provide the corresponding API key in the `OPENAI_TOKEN`
environment variable, and have enough credits.

<details>
<summary>SBT</summary>

```shell
env OPENAI_TOKEN=<your-token> sbt <your-command>
```
</details>

<details>
<summary>IntelliJ</summary>

Set the environment variable `OPENAI_TOKEN=xxx`

</details>

### Project Loom

The Scala module depends on project [Loom](https://openjdk.org/projects/loom/), 
so you will need at least Java 20 to use the library. Furthermore, if using Java 20 specifically,
you need to pass the `--enable-preview` flag.

<details>
<summary>SBT</summary>

```shell
env OPENAI_TOKEN=<your-token> sbt -J--enable-preview <your-command>
```
</details>

<details>
<summary>IntelliJ</summary>

- Set the Java version to at least 20
- If using Java 20 specifically, set VM options to `--enable-preview`

</details>

## Structure

The output from the `books` function above may be hard to parse back from the
strings we obtain. Fortunately, you can also ask xef.ai to give you back the information
using a _custom type_. The library takes care of instructing the LLM on building such
a structure, and deserialize the result back for you.

This can be done by declaring a case class that `derives SerialDescriptor, Decoder`:

```scala 3
import com.xebia.functional.xef.scala.conversation.*
import com.xebia.functional.xef.scala.serialization.*
import io.circe.Decoder

case class Book(name: String, author: String, pages: Int) derives SerialDescriptor, Decoder
```

The `conversation` block can then be written in this way:

```scala 3
def bookExample(topic: String): Unit = conversation:
  val Book(title, author, pages) = prompt[Book](s"Give me the best-selling book about $topic")
  println(s"The book $title is by $author and has $pages pages.")
```

xef.ai for Scala uses xef.ai core, which is based on the Kotlin implementation. Hence, the core 
reuses [Kotlin's common serialization](https://kotlinlang.org/docs/serialization.html), and
Scala uses [circe](https://github.com/circe/circe) to derive the required serializable instance. 
The LLM is usually able to detect which kind of information should go in each field based on its name 
(like `title` and `author` above).

## Context

LLMs have knowledge about a broad variety of topics. But by construction they are not able
to respond to questions about information not available in their training set. However, you
often want to supplement the LLM with more data:
- Transient information referring to the current moment, like the current weather, or
  the trends in the stock market in the past 10 days.
- Non-public information, for example for summarizing a piece of text you're creating
  within your organization.

These additional pieces of information are called the _context_ in xef.ai, and are attached
to every question to the LLM. Although you can add arbitrary strings to the context at any
point, the most common mode of usage is using an _agent_ to consult an external service,
and make its response part of the context. One such agent is `search`, which uses the
[Google Search API (SerpApi)](https://serpapi.com/) to enrich that context.

(Note that a SerpApi token may be required to run this example.)

```scala 3
import com.xebia.functional.xef.conversation.llm.openai.*
import com.xebia.functional.xef.reasoning.serpapi.*
import com.xebia.functional.xef.scala.conversation.*
import com.xebia.functional.xef.scala.serialization.*
import io.circe.Decoder

val openAI: OpenAI = OpenAI.FromEnvironment

def setContext(query: String)(using conversation: ScalaConversation): Unit =
  addContext(Search(openAI.DEFAULT_CHAT, conversation, 3).search(query).get)

@main def runWeather(): Unit = conversation:
  setContext("Weather in Cádiz, Spain")
  val question = "Knowing this forecast, what clothes do you recommend I should wear if I live in Cádiz?"
  val answer = promptMessage(question)
  println(answer)
```

> **Note**
> The underlying mechanism of the context is a _vector store_, a data structure which
> saves a set of strings, and is able to find those similar to another given one.
> By default xef.ai uses an _in-memory_ vector store, since this provides maximum
> compatibility across platforms. However, if you foresee your context growing above
> the hundreds of elements, you may consider switching to another alternative, like
> Lucene or PostgreSQL.

## Examples

Check out the 
[examples folder](https://github.com/xebia-functional/xef/blob/main/examples/scala/src/main/scala/com/xebia/functional/xef/examples) 
for a complete list of different use cases.
