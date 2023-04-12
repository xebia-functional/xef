# Search for similiar documents

This use case consists of using a vector store based on postgres to search for similar documents by preloading a file and using [OpenAI](https://openai.com/) embedding models to generate the embedding vectors.

## Description

First, the database configuration is defined including the `vectorSize` - the size of the embeddings column in the DB. The default value is `1536` as this is the length of the embeddings obtained by the default model (`text-embedding-ada-002`). Vector handling in postgres is provided by the extension [pgvector](https://github.com/pgvector/pgvector). The configuration for the OpenAI client is also defined using the default values. These values can be modified and even stored in environment variables and loaded by using the `Config.configValue[IO].load[IO]` method.

A txt file is then loaded using the specific loader for this type of file `TextLoader` using the `load` method. After this, the client for OpenAI and the interpreter to generate the embedding vectors through the OpenAI embedding model are generated. Next, the Postgres-specific VectorStore is generated.

Once the VectorStore is generated, a set of methods are executed to set up the database: `initialDbSetup` and `createCollection`. To add documents to the VectorStore we execute the `addTexts` method, which generates the embedding vectors and adds them to the DB. To retrieve the most similar documents to a particular query we use the `similaritySearch` method.

The `initialDbSetup`, `createCollection` and `addTexts` methods can be avoided if we use the `fromTexts` method. This method will return a VectorStore initialized from texts and embeddings.

## How to

To execute the example we have to take into account the following steps:

- Add the OpenAI API KEY in the `OPENAI_TOKEN` variable. You can also define an environment variable with this value and with the same name if you are going to use the method `Config.configValue[IO].load[IO]`.
- Run the docker compose that uses the Postgres image with the `pgvector` extension installed:
  + If you want to run the docker compose command from the project root directory, you would have to run the following command: `docker compose -f modules/examples/src/main/scala/com/xebia/functional/searchsimilardocs/docker-compose.yml up -d`
- Execute the use case by simply running it from your IDE of choice.