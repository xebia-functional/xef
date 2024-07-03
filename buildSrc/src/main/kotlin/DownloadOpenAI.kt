import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.net.URL
import java.nio.channels.Channels

@CacheableTask
abstract class DownloadOpenAI : DefaultTask() {
    init {
        description = "Download the OpenAI API client"
        group = "openai"
    }

    @get:InputFile
    @get:Option(description = "Commit hash of the fetched OpenAI OpenAPI Yaml file")
    abstract val input: RegularFileProperty

    @get:OutputFile
    @get:Option(description = "Path of file to store the fetched OpenAI OpenAPI Yaml")
    abstract val output: RegularFileProperty

    @TaskAction
    fun download() {
        val commit = readCommit()
        downloadAPI(commit)
    }

    private fun readCommit(): String =
        input.get().asFile.readText().trim { it <= ' ' }

    private fun downloadAPI(commit: String) {
        val url = URL("https://raw.githubusercontent.com/openai/openai-openapi/%s/openapi.yaml".formatted(commit))
        Channels.newChannel(url.openStream()).use { channel ->
            output.asFile.get().outputStream().use { output ->
                output.channel.transferFrom(channel, 0, Long.MAX_VALUE)
            }
        }
    }
}
