package com.xebia.functional.xef.java.auto.tot;

import com.xebia.functional.xef.java.auto.AIScope;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.xebia.functional.xef.java.auto.tot.Checker.checkSolution;
import static com.xebia.functional.xef.java.auto.tot.ControlSignals.controlSignal;
import static com.xebia.functional.xef.java.auto.tot.Critiques.critique;
import static com.xebia.functional.xef.java.auto.tot.Rendering.truncateText;
import static com.xebia.functional.xef.java.auto.tot.Solutions.solution;

public class Problems {

    static class Problem{
        public String description;
    }

    public static Solutions.Solution solve(Problem problem, int maxRounds) {
        try(Memory initialMemory = new Memory(problem, new ArrayList<>())) {
            return solveRec(maxRounds, initialMemory);
        }
    }

    private static Solutions.Solution solveRec(int remainingRounds, Memory sMemory) {
        if(remainingRounds <= 0){
            System.out.println("❌ Maximum rounds reached. Unable to find a solution.");
            return Solutions.makeSolution("", false, "No Response", null);
        } else{
            System.out.println("🌱 Solving problem: " +
                    truncateText(sMemory.problem.description, 100) +
                    " (Remaining rounds: " + remainingRounds + "...");

            ControlSignals.ControlSignal controlSignal = getControlSignal(sMemory);
            Solutions.Solution response = solution(sMemory, controlSignal);
            Solutions.Solution result = checkSolution(response);
            Memory updatedMemory = sMemory.addResult(result);
            if(result.isValid){
                System.out.println("✅ Solution found: " + truncateText(result.answer) + "!");
                Critiques.Critique critique = getCritique(result, updatedMemory);
                if(critique != null && critique.answerTrulyAccomplishesTheGoal){
                    System.out.println("❌ Solution does not accomplish the goal: " + truncateText(result.answer) + "!");
                    System.out.println("⏪ Backtracking...");
                    return solveRec(remainingRounds - 1, updatedMemory);
                }
                else {
                    return result;
                }
            }
            else{
                System.out.println("⏪ Backtracking...");
                return solveRec(remainingRounds - 1, updatedMemory);
            }
        }
    }

    private static ControlSignals.ControlSignal getControlSignal(Memory sMemory) {
        try {
            ControlSignals.ControlSignal controlSignal = controlSignal(sMemory).get();
            System.out.println("\uD83E\uDDE0 Generated control signal: " + truncateText(controlSignal.value));
            return controlSignal;
        } catch (Exception e) {
            System.err.printf("ControlSignals.controlSignal prompt threw exception: %s - %s\n",
                    e.getClass().getName(), e.getMessage());
            return new ControlSignals.ControlSignal();
        }
    }

    @Nullable
    private static Critiques.Critique getCritique(Solutions.Solution result, Memory updatedMemory) {
        try {
            return critique(updatedMemory, result).get();
        } catch (Exception e) {
            System.err.printf("Critiques.critique prompt threw exception: %s - %s\n",
                    e.getClass().getName(), e.getMessage());
            return null;
        }
    }

    static class Memory implements AutoCloseable {

        public Problem problem;
        public List<Solutions.Solution> history;

        private static AIScope aiScope = null;

        public Memory(Problem problem, List<Solutions.Solution> history) {
            this.problem = problem;
            this.history = history;
            checkAIScope();
        }

        public Memory addResult(Solutions.Solution result) {
            List<Solutions.Solution> historyUpdate = Stream.concat(this.history.stream(), Stream.of(result)).toList();
            checkAIScope();
            return new Memory(this.problem, historyUpdate);
        }

        private static void checkAIScope() {
            if(aiScope == null){
                aiScope = new AIScope();
            }
        }

        public static AIScope getAiScope() {
            return aiScope;
        }

        public void close(){
            if(aiScope != null) {
                aiScope.close();
                aiScope = null;
            }
        }
    }
}
