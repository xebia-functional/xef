package com.xebia.functional.xef.java.auto.tot;

public class Main {

    private static int MAX_ROUNDS = 5;

    public static void main(String[] args) {
        Problems.Problem problem = new Problems.Problem();
        problem.description = Rendering.trimMargin(
                "    You are an expert functional programmer.\n" +
                "    1. You never throw exceptions.\n" +
                "    2. You never use null.\n" +
                "    3. You never use `for` `while` or loops in general, prefer tail recursion.\n" +
                "    4. You never use mutable state.\n" +
                "    \n" +
                "    This code is unsafe. Find the problems in this code and provide a Github suggestion code fence with the `diff` to fix it.\n" +
                "    \n" +
                "    ```kotlin\n" +
                "    fun access(list: List<Int>, index: Int): Int {\n" +
                "      return list[index]\n" +
                "    }\n" +
                "    ```\n" +
                "    \n" +
                "    Return a concise solution that fixes the problems in the code.");


        Solutions.Solution<FinalSolution> solve = Problems.solve(problem, MAX_ROUNDS);

        System.out.println("✅ Final solution: " + solve.answer);
        System.out.println("✅ Solution validity: " + solve.isValid);
        System.out.println("✅ Solution reasoning: " + solve.reasoning);
        System.out.println("✅ Solution code: " + solve.value.solution);
    }

    static class FinalSolution {
        public String solution;
    }

}
