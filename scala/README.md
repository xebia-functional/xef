# xef.ai for Scala

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

Check out some use case at the [Scala examples](../examples/scala) folder.

### Running the Examples

How to run the examples (from IntelliJ IDEA):

* Set Java version 19
* Set VM options: "--enable-preview"
* Set Env variable: "OPENAI_TOKEN=xxx"
