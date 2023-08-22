# Quick introduction to xef.ai (Java version)

After adding the library to your project
(see the [main README](https://github.com/xebia-functional/xef/blob/main/README.md) for instructions),
you get access to the `AIScope` class, which is your port of entry to the modern AI world.
Using it, you can _prompt_ for information, which means posing the question to an LLM
(Large Language Model). The easiest way is to just get the information back as a string.

```java
package my.example;

import com.xebia.functional.xef.java.auto.AIScope;

import java.util.concurrent.ExecutionException;

public class Example {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            String topic = "artificial intelligence";
            scope.promptMessage("Give me a selection of books about " + topic)
                    .thenAccept(System.out::println)
                    .get();
        }
    }
}
```

> **Note**
> By default the `AIScope` connects to [OpenAI](https://platform.openai.com/).
> To use their services you should provide the corresponding API key in the `OPENAI_TOKEN`
> environment variable, and have enough credits.

In the example above we create an `AIScope` using the `try-with-resources` syntax,
which ensures that the scope is closed at the end of the block.
The `AIScope` gives us access to the `promptMessage` & co functions, which allow us to interact with the LLM.

All the functions of `AIScope` are returned as a `Future` for maximum backward compatibility until JDK8,
but you can inject `Executors.newVirtualThreadPerTaskExecutor()` to have the `Future`s work on virtual threads.

Remember that exceptions in `Future`are wrapped in `ExecutionException`,
so to inspect the actual exception you need to call `getCause()` on it.
_Structured Concurrency_ is implemented under the hood by Kotlin's `CoroutineScope`,
and all futures are cancelled when the `AIScope` is closed and `Future#get` will throw `CancellationException`.

In the next examples we'll write functions that rely on `AIScope`'s DSL functionality

## Structure


The output from the `books` function above may be hard to parse back from the
strings we obtain. Fortunately, you can also ask xef.ai to give you back the information
using a _custom type_. The library takes care of instructing the LLM on building such
a structure, and deserialize the result back for you.

We can thus define a `Book` class that describes the desired response we want to receive from the LLM.
Relying on [Jakarta validation](https://beanvalidation.org) we can also specify which fields are mandatory using `NotNull`,
or include additional constraints in the [Json Schema](https://json-schema.org).

xef.ai reuses [Jackson](https://github.com/FasterXML/jackson-databind),
and [JsonSchema generator](https://github.com/victools/jsonschema-generator) to parse and generate the Json Schema V7 for you.

```java
package my.example;

import jakarta.validation.constraints.NotNull;

public class Book {
  @NotNull public String title;
  @NotNull public String author;
  @NotNull public int year;
  @NotNull public String genre;

  @Override
  public String toString() {
    return "Book{" +
            "title='" + title + '\'' +
            ", author='" + author + '\'' +
            ", year=" + year +
            ", genre='" + genre + '\'' +
            '}';
  }
}
```

Using the definition of `Book`, we can rewrite our previous example as:

```java
package my.example;

import com.xebia.functional.xef.java.auto.AIScope;
import jakarta.validation.constraints.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Example {

    private final AIScope scope;

    public Example(AIScope scope) {
        this.scope = scope;
    }
    
    public CompletableFuture<Book> bookSelection(String topic) {
        return scope.prompt("Give me a selection of books about " + topic, Example.Book.class);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            Example example = new Example(scope);
            example.bookSelection("artificial intelligence")
                    .thenAccept(System.out::println)
                    .get();
        }
    }
}
```

Here we also show how you can easily capture the `AIScope` in a class,
and build and compose additional functionality on top.
If you're using any dependency injection framework, you can also construct `AIScope` and inject it as usual.
Make sure that the dependency injection framework properly closes the `AIScope` when the application shuts down.

## Context

LLMs have knowledge about a broad variety of topics. But by construction they are not able
to respond to questions about information not available in their training set. However, you
often want to supplement the LLM with more data:
- Transient information referring to the current moment, like the current weather, or
  the trends in the stock market in the past 10 days.
- Non-public information, for example for summarizing a piece of text you're creating
  within you organization.

These additional pieces of information are called the _contextScope_ in xef.ai, and are attached
to every question to the LLM. Although you can add arbitrary strings to the context at any
point, the most common mode of usage is using an _agent_ to consult an external service,
and make its response part of the context. One such agent is `search`, which uses a web
search service to enrich that context.

```java
package my.example;

import java.util.concurrent.CompletableFuture;

public class Weather {
    private final AIScope scope;

    public Weather(AIScope scope) {
        this.scope = scope;
    }

    public CompletableFuture<String> recommendation() {
        return scope.contextScope(scope.search("Weather in $place"), () ->
                scope.promptMessage("Knowing this forecast, what clothes do you recommend I should wear?")
        );
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
> ```java
> package my.example;
> 
> import com.xebia.functional.xef.store.LuceneKt;
> 
> import java.nio.file.Path;
> import java.util.concurrent.CompletableFuture;
> 
> public class VectorStore {
>
>     private final AIScope scope;
> 
>     public VectorStore(AIScope scope) {
>         this.scope = scope;
>     }
>
>     public void example() {
>         Path LUCENE_PATH = Path.of("lucene");
>         scope.contextScope(
>             LuceneKt.InMemoryLuceneBuilder(LUCENE_PATH),
>             () -> CompletableFuture.completedFuture("do stuff")
>         );
>     }
> 
> }
> ```
