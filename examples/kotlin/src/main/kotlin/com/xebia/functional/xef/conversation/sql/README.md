# How to run the SQL example
Shows how to use the xef-sql integration module.

For this example we are using a docker container with a mysql database with some data to test it.

# Pre-requisites
- docker (https://docs.docker.com/install/) 
- docker-compose (https://docs.docker.com/compose/install/)

# Steps to run

### Build and  up the mysql container
`./gradlew xef-kotlin-examples:docker-sql-example-up`

### Create the database and populate it with some data:
`./gradlew xef-kotlin-examples:docker-sql-example-create`
 
### Set OPENAI_TOKEN and run the example
`env OPENAI_TOKEN=<your-token> ./gradlew xef-kotlin-examples:run-sql-example`
