package com.xebia.functional.xef.java.auto.jdk8.tot;

import com.xebia.functional.xef.auto.llm.openai.OpenAI;

import static com.xebia.functional.xef.java.auto.jdk8.tot.Rendering.renderHistory;
import static com.xebia.functional.xef.java.auto.jdk8.tot.Rendering.truncateText;

public class Solutions{

    static class Solution<A>{
        public String answer;
        public boolean isValid;
        public String reasoning;

        public A value;
    }

    public static <B> Solution<B> makeSolution(String answer, boolean isValid, String reasoning, B value) {
        Solution<B> solution = new Solution<>();
        solution.isValid = isValid;
        solution.answer = answer;
        solution.reasoning = reasoning;
        solution.value = value;
        return solution;
    }

    public static <C> Solution<C> solution(Problems.Memory<C> memory,
                                        ControlSignals.ControlSignal controlSignal){
        //ai emoji
        System.out.println("\uD83E\uDD16 Generating solution for problem: " + truncateText(memory.problem.description) + "...");

        String enhancedPrompt =
                "       Given previous history:\n" +
                "       " + renderHistory(memory) + "\n" +
                "       Given the goal: \n" +
                "       ```goal\n" +
                "       " + memory.problem.description + " \n" +
                "       ```\n" +
                "       and considering the guidance: \n" +
                "       ```guidance\n" +
                "       " + controlSignal.value + "\n" +
                "       ```\n" +
                "       \n" +
                "       Instructions:\n" +
                "       \n" +
                "       1. Please provide a comprehensive solution. \n" +
                "       2. Consider all possible scenarios and edge cases. \n" +
                "       3. Ensure your solution is accurate, complete, and unambiguous. \n" +
                "       4. If you are unable to provide a solution, please provide a reason why and set `isValid` to false.\n" +
                "       5. Include citations, references and links at the end to support your solution based on your sources.\n" +
                "       6. Do not provide recommendations, only provide a solution.\n" +
                "       7. when `isValid` is true Include in the `value` field the value of the solution according to the `value` json schema.\n" +
                "       8. If no solution is found set the `value` field to `null`.\n" +
                "       9. If the solution is not valid set the `isValid` field to `false` and the `value` field to `null`.\n" +
                "       10. If the solution is valid set the `isValid` field to `true` and the `value` field to the value of the solution.\n" +
                "       \n";

        try {
            return Problems.Memory.getAiScope().prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, enhancedPrompt, Solution.class).get();
        } catch (Exception e) {
            System.err.printf("Solutions.solution enhancedPrompt threw exception: %s - %s\n",
                    e.getClass().getName(), e.getMessage());
            return null;
        }
    }

}
