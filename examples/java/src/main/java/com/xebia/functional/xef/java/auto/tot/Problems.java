package com.xebia.functional.xef.java.auto.tot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.xebia.functional.xef.java.auto.tot.ControlSignals.controlSignal;
import static com.xebia.functional.xef.java.auto.tot.Rendering.truncateText;

public class Problems {

    static class Problem{
        public String description;
    }

    public static <S> Solutions.Solution<S> solve(Problem problem, int maxRounds) {
        Memory<S> initialMemory = new Memory<>(problem, new ArrayList<>());
        return solveRec(problem, maxRounds, initialMemory);
    }

    private static <S> Solutions.Solution<S> solveRec(Problem problem, int remainingRounds, Memory<S> sMemory) {
        if(remainingRounds <= 0){
            System.out.println("❌ Maximum rounds reached. Unable to find a solution.");
            return Solutions.makeSolution("", false, "No Response", null);
        } else{
            System.out.println("🌱 Solving problem: " +
                    truncateText(sMemory.problem.description, 100) +
            " (Remaining rounds: $remainingRounds)...");

            ControlSignals.ControlSignal controlSignal = controlSignal(sMemory);
            Solutions.Solution<S> response = Solutions.solution(null, sMemory, controlSignal);
            Solutions.Solution<S> result = Checker.checkSolution(response);
            Memory updatedMemory = sMemory.addResult(result);
            if(result.isValid){
                //TODO
            }
        }
        return null;
    }

    static class Memory<A> {

        public Problem problem;
        public List<Solutions.Solution<A>> history = new ArrayList<>();

        public Memory(Problem problem, List<Solutions.Solution<A>> history) {
            this.problem = problem;
            this.history = history;
        }

        public Memory<A> addResult(Solutions.Solution<A> result) {
            List<Solutions.Solution<A>> historyUpdate = Stream.concat(this.history.stream(), Stream.of(result)).toList();
            return new Memory<A>(this.problem, historyUpdate);
        }

    }
}
