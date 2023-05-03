# 🦜️🔗 LangChain4s

Build the project locally, from the project root:

```bash
./gradlew build
```

## Scalafmt

The Scala module uses the [spotless](https://github.com/diffplug/spotless/tree/main/plugin-gradle#scala) plugin. 
Therefore, the previous command (`./gradlew build`) will fail in case there is any formatting issue. To apply format, you can run the following command:

```bash
./gradlew spotlessApply
```

## Examples

The following use cases show in a simplified way different functionality of LangChain4s:

+ [Generate embeddings](completionembedding/README.md): embedding vector generation for a query or a set of texts using a specific embedding model.
+ [Search for similar documents](searchsimilardocs/README.md): search for documents most similar to a query using embedding vectors stored in a vector store.
+ [Build a QA System](qasystem/README.md): Question-Answering system using a VectorStore.