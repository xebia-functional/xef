package com.xebia.functional.xef.java.auto;

public class ASCIIArt {
    public static class Art {
        public String art;
    }

    public static void main(String[] args) {
        try (AIScope scope = new AIScope()) {
            Art art = scope.prompt("ASCII art of a cat dancing", Art.class);
            System.out.println(art.art);
        }
    }
}
