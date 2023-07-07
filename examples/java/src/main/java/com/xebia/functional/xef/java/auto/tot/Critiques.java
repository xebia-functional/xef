package com.xebia.functional.xef.java.auto.tot;

import java.util.concurrent.CompletableFuture;

import static com.xebia.functional.xef.java.auto.tot.Rendering.truncateText;

public class Critiques {

    static class Critique {
        String answer;
        String reasoning;
        boolean answerTrulyAccomplishesTheGoal;
    }

    public static <A> CompletableFuture<Critique> critique(Problems.Memory<A> memory, Solutions.Solution<A> currentSolution){
        System.out.println("🕵️ Critiquing solution: " + truncateText(currentSolution.answer) + "...");

        String prompt =
                "    You are an expert advisor critiquing a solution.\n" +
                "    \n" +
                "    Previous history:\n" +
                "    " + Rendering.renderHistory(memory) + "\n" +
                "    \n" +
                "    You are given the following problem:\n" +
                "    " + memory.problem.description + "\n" +
                "    \n" +
                "    You are given the following solution:\n" +
                "    " + currentSolution.answer + "\n" +
                "    \n" +
                "    Instructions:\n" +
                "    1. Provide a critique and determine if the answer truly accomplishes the goal.\n" +
                "    \n" +
                "  ";

        return memory.getAiScope().prompt(prompt, Critique.class);
    }
}
