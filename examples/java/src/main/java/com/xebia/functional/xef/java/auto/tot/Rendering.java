package com.xebia.functional.xef.java.auto.tot;

public class Rendering {

    public static String truncateText(String answer) {
        return truncateText(answer, 150);
    }

    public static String truncateText(String answer, int i) {
        if(answer != null && answer.length() > i) {
            return answer.substring(0, i);
        }
        return answer;
    }
}
