# Xef Server

The server module for the xef project.

## Run Server

In order to run the server, you need to run the following services:

### Docker

```shell
docker-compose -f docker/postgresql/docker-compose.yaml up 
```

### Server

```shell
./gradlew server
```

### Web

Please refer to the [web README](web/README.md) for more information.
