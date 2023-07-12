package com.xebia.functional.xef.java.auto;

import java.util.concurrent.ExecutionException;

public class Movies {

    public static class Movie {
        public String title;
        public String genre;
        public String director;

        @Override
        public String toString() {
            return "The movie " + title + " is a " +
                    genre + " film directed by " + director + ".";
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            scope.prompt("Please provide a movie title, genre and director for the Inception movie", Movie.class)
                    .thenAccept(movie -> System.out.println(movie))
                    .get();
        }
    }
}
