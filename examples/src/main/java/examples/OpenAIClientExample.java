package examples;

import com.xebia.functional.openai.generated.model.*;
import com.xebia.functional.openai.generated.model.ChatCompletionRequestUserMessageContent.CaseString;
import com.xebia.functional.xef.OpenAI;

import java.util.List;

public class OpenAIClientExample {

    public static void main(String[] args) {
        var openAI = OpenAI.create();
        var chat = openAI.getChat();
        var response = chat.createChatCompletionBlocking(new CreateChatCompletionRequest(
                        List.of(ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage.create(
                                new ChatCompletionRequestUserMessage(
                                        CaseString.create("Hello, how are you?"),
                                        ChatCompletionRequestUserMessage.Role.user
                                )
                        )),
                        CreateChatCompletionRequestModel.Supported.gpt_3_5_turbo
                ));
        System.out.println(response.getChoices().get(0).getMessage().getContent());
    }
}
