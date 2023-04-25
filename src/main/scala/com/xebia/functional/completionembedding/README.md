# Generate embeddings

This use case consists of generating an answer to a question and a vector of embeddings from a text using an embedding model and an [OpenAI](https://openai.com/) client.

## Description

First, the OpenAI client configuration is defined. This `OpenAIClient` will be used to make the different calls for each of the purposes of the use case. These values can be modified and even stored in environment variables and loaded by using `Config.configValue[IO].load[IO]`.

Once the client is generated, we are going to use it. The first use case is triggered by the `openAIExample` function, in which we are using the `createCompletion` method that will send the request to the OpenAI API using the specifications included in `CompletionRequest`. This will return a list of `OpenAICompletions`. Some of these specifications are the model to use and the prompt including the query. In the `CompletionRequest` you can modify many more specifications.

The second use case is the one defined in the `openAIExample` function. In this one, we will use the `createEmbeddings` method to send a request to generate an embedding vector indicating the specifications through an `EmbeddingRequest`. Through this, we can indicate the embedding model and the text we want to process. This process will return an `EmbeddingResult`.

Once the two requests have been made, we store them in `o1` and `o2`, which are printed in the console.

## How to

To execute the example we have to take into account the following steps:

- Add the OpenAI API KEY in the `OPENAI_TOKEN` variable. You can also define an environment variable with this value and with the same name if you are going to use the method `Config.configValue[IO].load[IO]`.
- Execute the use case by simply running it from your IDE of choice.