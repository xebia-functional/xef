import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
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
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val input: RegularFileProperty

    @get:OutputFile
    @get:Option(description = "Path of file to store the fetched OpenAI OpenAPI Yaml")
    abstract val output: RegularFileProperty

    @TaskAction
    fun download() {
        val commit = readCommit()
        downloadAPI(commit)
    }

    private fun readCommit(): String {
        val asFile = input.get().asFile
        val readText = asFile.readText()
        require(readText.isNotEmpty()) { "${asFile.path} Commit hash is empty" }
        return readText.trim { it <= ' ' }
    }

    private fun downloadAPI(commit: String) {
        val url = URL("https://raw.githubusercontent.com/openai/openai-openapi/$commit/openapi.yaml")
        Channels.newChannel(url.openStream()).use { channel ->
            output.asFile.get().outputStream().use { output ->
                output.channel.transferFrom(channel, 0, Long.MAX_VALUE)
            }
        }
    }
}
