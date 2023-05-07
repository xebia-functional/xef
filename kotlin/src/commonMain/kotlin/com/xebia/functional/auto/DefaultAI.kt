package com.xebia.functional.auto

import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.fx.coroutines.Atomic
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.parMapOrAccumulate
import arrow.fx.coroutines.parZipOrAccumulate
import com.xebia.functional.auto.serialization.JsonType
import com.xebia.functional.auto.serialization.jsonType
import com.xebia.functional.chains.VectorQAChain
import com.xebia.functional.llm.openai.ChatCompletionRequest
import com.xebia.functional.llm.openai.ChatCompletionResponse
import com.xebia.functional.llm.openai.Message
import com.xebia.functional.llm.openai.Role
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.*

@OptIn(ExperimentalSerializationApi::class)
fun SerialDescriptor.sample(): JsonElement {
    val properties = elementNames.zip(elementDescriptors).associate { (name, descriptor) ->
        name to when (descriptor.kind.jsonType) {
            JsonType.ARRAY -> JsonArray(listOf(descriptor.sample()))
            JsonType.NUMBER -> JsonUnquotedLiteral("<number>")
            JsonType.STRING -> JsonUnquotedLiteral("<string>")
            JsonType.BOOLEAN -> JsonUnquotedLiteral("<true | false>")
            JsonType.OBJECT -> descriptor.sample()
            JsonType.OBJECT_SEALED -> descriptor.sample()
            JsonType.OBJECT_MAP -> descriptor.sample()
        }
    }
    return JsonObject(properties)
}

fun ResourceScope.DefaultAI(config: Config): AI = object : AI() {

    override val config: Config = config

    override val agents: Atomic<List<Agent>> = Atomic.unsafe(emptyList())

    override suspend operator fun <A> Raise<AIError>.invoke(
        prompt: String, serializationConfig: SerializationConfig<A>, auto: Boolean,
        maxAttempts: Int
    ): A {
        // here we create a VectorQAChain where agents are the tools
        // that provide additional context to the vectorStore to answer the question
        val augmentedPrompt = """
                |Objective: $prompt
                |Instructions: Use the following JSON schema to produce the result in JSON format
                |JSON Schema:
                |${serializationConfig.jsonSchema}
                |Response Example:
                |${serializationConfig.descriptor.sample()}
                |Response:
            """.trimMargin()
        val result = if (auto) {
            // immediately reset automode because it's only for one call
            logger.debug { "Solving objective in auto reasoning mode" }
            val resolutionContext: Solution = solveObjective(prompt, 5)
            val promptWithContext = """
                    |${resolutionContext.result}
                    |
                    |Given this information solve the objective:
                    |
                    |$augmentedPrompt
                """.trimMargin()
            openAIChatCall(prompt, promptWithContext)
        } else {
            logger.debug { """
                |Solving objective in manual reasoning mode
                |```
                |$augmentedPrompt
                |```
            """.trimMargin() }
            openAIChatCall(prompt, augmentedPrompt)
        }
        logger.debug { """|
            |Result:
            |```
            |$result
            |```
        """.trimMargin() }
        return catch({
            json.decodeFromString(serializationConfig.deserializationStrategy, result)
        }) { e ->
            if (maxAttempts <= 0) raise(AIError(result))
            else {
                logger.debug { "Error deserializing result, trying again..." }
                invoke(prompt, serializationConfig, false, maxAttempts - 1).also { logger.debug { "Fixed JSON: $it" } }
            }
        }
    }

    tailrec suspend fun Raise<AIError>.solveObjective(
        objective: String,
        maxAttempts: Int
    ): Solution = if (maxAttempts <= 0) {
        raise(AIError("Max attempts reached"))
    } else {
        val enhancedPrompt = """
            |You are an expert AI that can help us solve a task.
            |Instructions: Only set `objectiveAccomplished` to `true` when you are certain that the objective is accomplished.
            |In the event that you are not sure whether the objective is accomplished, you can set `objectiveAccomplished` to `false` and provide additional tasks and ideas in the `result` field that would help accomplish the objective.
            |You are currently evaluated in a loop self reasoning, try to provide the best solution possible and otherwise provide 
            |instructions as to how to accomplish further steps to achieve the task completion.
            |
            |Task: $objective
        """.trimMargin()
        val solution: Solution = ai(enhancedPrompt)
        if (solution.objectiveAccomplished) {
            val additionalTasks: AdditionalTasks = ai(objective)
            val reassurance: Reassurance = ai(
                """
                |You are an expert AI that can help us ensure we have a valid solution for this Objective.
                |Result: ${solution.result}
                |Objective: ${additionalTasks.objective}
                |Tasks if `objectiveAccomplished = false`: 
                |${additionalTasks.tasks.joinToString("\n")}
            """.trimIndent()
            )
            if (reassurance.objectiveAccomplished) solution
            else {
                val results: List<Solution> = reassurance.tasksWouldHelpAccomplishObjective.filter { (_, t) ->
                    t
                }.map { (task, _) ->
                    ai(task)
                }
                results.fold(solution) { acc, s ->
                    if (s.objectiveAccomplished && acc.objectiveAccomplished)
                        Solution("${acc.objective}\n\n${s.objective}", acc.result + "\n\n" + s.result, true)
                    else acc
                }
            }
        } else {
            val additionalTasks: AdditionalTasks = ai(objective)
            val solutions = processAdditionalTasks(additionalTasks, maxAttempts - 1)
            findBestSolution(solutions)
        }
    }

    suspend fun Raise<AIError>.processAdditionalTasks(
        additionalTasks: AdditionalTasks,
        maxAttempts: Int
    ): List<Solution> =
        additionalTasks.tasks.parMapOrAccumulate {
            solveObjective(additionalTasks.objective, maxAttempts - 1)
        }.getOrElse { raise(AIError(it.joinToString())) }


    fun Raise<AIError>.findBestSolution(solutions: List<Solution>): Solution {
        // Implement a logic to select the best solution from the solutions list.
        // Here, we simply return the first solution that accomplishes the objective or a default one.
        return solutions.firstOrNull { it.objectiveAccomplished }
            ?: raise(AIError("No solution found after tasks refinement for solutions: $solutions"))
    }

    private suspend fun Raise<AIError>.openAIChatCall(
        question: String,
        promptWithContext: String
    ): String {
        //run the agents so they store context in the database
        agents.get().forEach { agent ->
            agent.storeResults(question, config.vectorStore)
        }
        //run the vectorQAChain to get the answer
        val numOfDocs = 10
        val outputVariable = "answer"
        val chain = VectorQAChain(
            config.openAIClient,
            config.vectorStore,
            numOfDocs,
            outputVariable
        )
        val chainResults: Map<String, String> = chain.run(question).getOrElse { raise(AIError(it.reason)) }
        logger.debug { "Chain results: $chainResults" }
        val promptWithMemory =
            if (chainResults.isNotEmpty())
                """
                |Instructions: Use the [Information] below delimited by 3 backticks to accomplish
                |the [Objective] at the end of the prompt.
                |Try to match the data returned in the [Objective] with this [Information] as best as you can.
                |[Information]:
                |```
                |${chainResults.entries.joinToString("\n") { (k, v) -> "$k: $v" }}
                |```
                |[Objective]:
                |$promptWithContext
                """.trimMargin()
            else promptWithContext
        logger.debug { "Prompt with memory: \n$promptWithMemory" }
        val res = chatCompletionResponse(promptWithMemory, "gpt-3.5-turbo", "AI_Value_Generator")
        val msg = res.choices.firstOrNull()?.message?.content
        requireNotNull(msg) { "No message found in result: $res" }
        return msg
    }

    private suspend fun chatCompletionResponse(
        prompt: String, model: String, user: String
    ): ChatCompletionResponse {
        val completionRequest = ChatCompletionRequest(
            model = model, messages = listOf(Message(Role.system.name, prompt, user)), user = user
        )
        return config.openAIClient.createChatCompletion(completionRequest)
    }
}

