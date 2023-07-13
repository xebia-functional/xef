package com.xebia.functional.xef.java.auto.tot;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Rendering {

    public static String trimMargin(String input){
        return Arrays.stream(input.split("\n")).map(String::trim).collect(Collectors.joining("\n"));
    }

    public static String truncateText(String answer) {
        return truncateText(answer, 150);
    }

    public static String truncateText(String answer, int limit) {
        if(answer == null)
            return "<Empty Answer>";
        if(answer.length() > limit) {
            answer = answer.substring(0, limit - 3) + "...";
        }
        return answer.replace("\n", " ");
    }

    public static <A> String renderHistory(Problems.Memory<A> memory){
        return trimMargin("   ```history \n\n" +
                memory.history.stream()
                .map(Rendering::renderHistoryItem)
                .collect(Collectors.joining("\n")) +
                "```");
    }

    private static <S> String renderHistoryItem(Solutions.Solution<S> solution){
        return trimMargin(solution.answer + "\n" +
                solution.reasoning + "\n" +
                (solution.isValid ? "✅" : "❌") + "\n");
    }
}
