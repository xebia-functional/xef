# Build a QA System

This use case consists of building a QA system by enriching it with a context extracted from a text file and stored in a VectorStore. In this case we will use the [Postgres VectorStore](https://github.com/pgvector/pgvector) and [OpenAI](https://openai.com/).

## Description

First, we configure the different components. In this case, we only need to specify the configuration of the OpenAI client, which will be in charge of making the requests for both the LLM prediction and the creation of the embeddings, and the configuration of the database.

After this, all steps of the QA system construction are performed in a for comprehension. In this, we load the text file that has the relevant information for our context via the `TextLoader`, generate the OpenAi client and embeddings, and build the vector store directly from the documents extracted from the file. In this way, using the `fromDocuments` method, we generate an `PGVectorStore` with the vectorised documents already stored in the database.

Finally, we just need to create our `RetrievalQAChain` from the required elements. In this case, the `makeWithDefaults` method is used which generates the `RetrievalQAChain` with default values suitable for the purpose, but these can be modified using the `make` method instead. All that remains is to launch the question via the `run` method.

## How to

To execute the example we have to take into account the following steps:

- Add the OpenAI API KEY in the `OPENAI_TOKEN` variable. You can also define an environment variable with this value and with the same name if you are going to use the method `Config.configValue[IO].load[IO]`.
- Run the docker compose that uses the Postgres image with the `pgvector` extension installed:
  + If you want to run the docker compose command from the project root directory, you would have to run the following command: `docker compose -f modules/examples/src/main/scala/com/xebia/functional/qasystem/docker-compose.yml up -d`
- Execute the use case by simply running it from your IDE of choice.
