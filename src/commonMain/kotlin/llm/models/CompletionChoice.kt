package llm.models

//import com.theokanning.openai.completion.CompletionChoice as JCompletionChoice
data class CompletionChoice(val text: String, val index: Int, val finishReason: String){
//    companion object
//        fun fromJava(j: JCompletionChoice): Unit = Unit
}
