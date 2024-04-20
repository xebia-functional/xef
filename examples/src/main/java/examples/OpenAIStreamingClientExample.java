package examples;

import com.xebia.functional.openai.StreamCallback;
import com.xebia.functional.openai.generated.model.*;
import com.xebia.functional.openai.generated.model.ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage;
import com.xebia.functional.openai.generated.model.ChatCompletionRequestUserMessageContent.CaseString;
import com.xebia.functional.xef.OpenAI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.xebia.functional.openai.generated.model.ChatCompletionRequestUserMessage.Role.user;
import static com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel.Supported.gpt_3_5_turbo;

public class OpenAIStreamingClientExample {

    public static void main(String[] args) {
        var openAI = OpenAI.create();
        var chat = openAI.getChat();

        chat.createChatCompletionStream(new CreateChatCompletionRequest(
                List.of(CaseChatCompletionRequestUserMessage.create(
                        new ChatCompletionRequestUserMessage(
                                CaseString.create("Write a Java program to reverse a string."),
                                user
                        )
                )),
                gpt_3_5_turbo
        ), StreamCallback.<CreateChatCompletionStreamResponse> create(
                response -> {
                    System.out.print(response.getChoices().get(0).getDelta().getContent());
                    return null;
                }
        ));
    }
}
