package com.xebia.functional.xef.java.auto.tot;

import java.util.stream.Collectors;

public class Rendering {

    public static String truncateText(String answer) {
        return truncateText(answer, 150);
    }

    public static String truncateText(String answer, int limit) {
        if(answer == null)
            return "Empty answer";
        if(answer.length() > limit) {
            answer = answer.substring(0, limit - 3) + "...";
        }
        return answer.replace("\n", " ");
    }

    public static <A> String renderHistory(Problems.Memory<A> memory){
        return "   history \n\n" +
        memory.history.stream()
                .map(it -> renderHistoryItem(it))
                .collect(Collectors.joining("\n"));
    }

    private static <S> String renderHistoryItem(Solutions.Solution<S> solution){
        return solution.answer + "\n" +
                solution.reasoning + "\n" +
                (solution.isValid ? "✅" : "❌") + "\n";
    }
}
