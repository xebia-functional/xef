package com.xebia.functional.xef.java.auto.tot;

public class Checker {

    public static <A> Solutions.Solution<A> checkSolution(Solutions.Solution<A> response){
        System.out.println("✅ Validating solution: " + Rendering.truncateText(response.answer) + "...");
        return response.isValid ? response :
                Solutions.makeSolution(response.answer, false, "Invalid solution", null);
    }
}
