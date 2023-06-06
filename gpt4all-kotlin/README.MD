# gpt4all-kotlin

Kotlin multiplatform bindings for GP4All.

Right now, only `Kotlin/JVM` platform is supported via JNA.

# Configuration

By default, the library is using last model backend built from: [c5de9634c90bba5b528a6fceebc9a48a7f2e2597](https://github.com/nomic-ai/gpt4all/tree/c5de9634c90bba5b528a6fceebc9a48a7f2e2597/gpt4all-backend), which is included inside `src/commonMain/resources` folder.

Right now, only `darwin-aarch64` and `linux-x86-64` platforms are included.

It is also possible to use a GPT4All local installation by setting the `jna.library.path` system property to the path where the model backend is located: `<YOUR_GPT4ALL_INSTALLATION_FOLDER>/lib`

# How to use

In order to use the library, apart from the previous configuration, you must have downloaded some GPT4All model in your local, and pass both the model path and the model type:

```
GPT4All(Path.of("models/gpt4all/ggml-gpt4all-j-v1.3-groovy.bin"), GPT4AllModel.Type.GPTJ).use { gpt4All ->
                val promptMessage = Message(Message.Role.USER, "Some prompt goes here")
                gpt4All.chatCompletion(listOf(promptMessage))
}
```
