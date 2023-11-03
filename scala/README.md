# xef.ai for Scala

Build the project locally, from the project root:

```shell
./gradlew build
```

## Scalafmt

The Scala module uses the [spotless](https://github.com/diffplug/spotless/tree/main/plugin-gradle#scala) plugin. 
Therefore, the previous command (`./gradlew build`) will fail if there are any formatting issues. 
To apply formatting, you can run the following command:

```shell
./gradlew spotlessApply
```

## Examples

Check out some use case at the [Scala examples](../examples/scala) folder.

### Running the Examples

How to run the examples (from IntelliJ IDEA):

* Set Java version 20 or above
* Set VM options: `--enable-preview` (if using Java 20 specifically)
* Set Env variable: `OPENAI_TOKEN=xxx`
