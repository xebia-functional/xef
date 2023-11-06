# xef.ai for Java

Build the project locally, from the project root:

```shell
./gradlew build
```

## Java Spotless

The Java module uses the [spotless](https://github.com/diffplug/spotless/tree/main/plugin-gradle#java) plugin. 
Therefore, the previous command (`./gradlew build`) will fail in case there is any formatting issue. To apply format, you can run the following command:

```shell
./gradlew spotlessApply
```

## Examples

Check out some use case at the [Java examples](../examples/java) folder.

### Running the Examples

How to run the examples (from IntelliJ IDEA):

* Set Env variable: `OPENAI_TOKEN=xxx`
