package com.xebia.functional.xef.java.auto;

public class Animals {
    private static class Animal {
        public String name;
        public String habitat;
        public String diet;
    }

    private static class Invention {
        public String name;
        public String inventor;
        public int year;
        public String purpose;
    }

    private static class Story {
        public Animal animal;
        public Invention invention;
        public String story;
    }

    public static void main(String[] args) {
        AIScope.run((scope) -> {
            Animal animal = scope.prompt("A unique animal species.", Animal.class);
            Invention invention = scope.prompt("A groundbreaking invention from the 20th century.", Invention.class);
            String storyPrompt =
                    "Write a short story of 500 words that involves the following elements:" +
                            "1. A unique animal species called ${animal.name} that lives in " + animal.habitat + " and has a diet of " + animal.diet + "." +
                            "2. A groundbreaking invention from the 20th century called " + invention.name + " , invented by " + invention.inventor + " in " + invention.year + ", which serves the purpose of " + invention.purpose + ".";
            Story story = scope.prompt(storyPrompt, Story.class);
            System.out.println("Story about " + animal.name + " and " + invention.name + ": " + story.story);
            return null;
        });
    }
}
