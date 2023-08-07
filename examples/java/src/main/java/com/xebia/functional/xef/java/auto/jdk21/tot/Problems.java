package com.xebia.functional.xef.java.auto.jdk21.tot;

import static com.xebia.functional.xef.java.auto.jdk21.tot.Rendering.truncateText;

import com.xebia.functional.xef.java.auto.AIScope;
import com.xebia.functional.xef.java.auto.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

public class Problems {

    static class Problem{
        public String description;
    }

    public static <S> Solutions.Solution<S> solve(Problem problem, int maxRounds) {
        try(Memory<S> initialMemory = new Memory<>(problem, new ArrayList<>())) {
            return solveRec(maxRounds, initialMemory);
        }
    }

    private static <S> Solutions.Solution<S> solveRec(int remainingRounds, Memory<S> sMemory) {
        if(remainingRounds <= 0){
            System.out.println("❌ Maximum rounds reached. Unable to find a solution.");
            return Solutions.makeSolution("", false, "No Response", null);
        } else{
            System.out.println("🌱 Solving problem: " +
                    truncateText(sMemory.problem.description, 100) +
                    " (Remaining rounds: " + remainingRounds + "...");

            ControlSignals.ControlSignal controlSignal = getControlSignal(sMemory);
            Solutions.Solution<S> response = Solutions.solution(sMemory, controlSignal);
            Solutions.Solution<S> result = Checker.checkSolution(response);
            Memory<S> updatedMemory = sMemory.addResult(result);
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

    private static <S> ControlSignals.ControlSignal getControlSignal(Memory<S> sMemory) {
        try {
            ControlSignals.ControlSignal controlSignal = ControlSignals.controlSignal(sMemory).get();
            System.out.println("\uD83E\uDDE0 Generated control signal: " + truncateText(controlSignal.value));
            return controlSignal;
        } catch (Exception e) {
            System.err.printf("ControlSignals.controlSignal prompt threw exception: %s - %s\n",
                    e.getClass().getName(), e.getMessage());
            return new ControlSignals.ControlSignal();
        }
    }

    @Nullable
    private static <S> Critiques.Critique getCritique(Solutions.Solution<S> result, Memory<S> updatedMemory) {
        try {
            return Critiques.critique(updatedMemory, result).get();
        } catch (Exception e) {
            System.err.printf("Critiques.critique prompt threw exception: %s - %s\n",
                    e.getClass().getName(), e.getMessage());
            return null;
        }
    }

    static class Memory<A> implements AutoCloseable {

        public Problem problem;
        public List<Solutions.Solution<A>> history;

        private static AIScope aiScope = null;

        public Memory(Problem problem, List<Solutions.Solution<A>> history) {
            this.problem = problem;
            this.history = history;
            checkAIScope();
        }

        public Memory<A> addResult(Solutions.Solution<A> result) {
            List<Solutions.Solution<A>> historyUpdate = Stream.concat(this.history.stream(), Stream.of(result)).toList();
            checkAIScope();
            return new Memory<>(this.problem, historyUpdate);
        }

        private static void checkAIScope() {
            if(aiScope == null){
                aiScope = new AIScope(new ExecutionContext(Executors.newSingleThreadExecutor()));
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
