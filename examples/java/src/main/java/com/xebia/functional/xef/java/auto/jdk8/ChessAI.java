package com.xebia.functional.xef.java.auto.jdk8;

import com.xebia.functional.xef.java.auto.AIScope;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ChessAI {

    private static class ChessMove {
        public String player;
        public String move;

        @Override
        public String toString() {
            return "ChessMove{" +
                  "player='" + player + '\'' +
                  ", move='" + move + '\'' +
                  '}';
        }
    }

    private static class ChessBoard {
        public String board;
    }

    private static class GameState {
        public Boolean ended;
        public String winner;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AIScope scope = new AIScope()) {
            var moves = new ArrayList<ChessMove>();
            var gameEnded = false;
            var winner = "";

            while (!gameEnded) {
                var currentPlayer = ((moves.size() % 2) == 0) ? "Player 1 (White)" : "Player 2 (Black)";

                var prompt = String.format("""
                            |%s, it's your turn.
                            |Previous moves: %s
                            |Make your next move:""",
                      currentPlayer,
                      moves.stream().map(ChessMove::toString).collect(Collectors.joining(", ")));

                ChessMove move = scope.prompt(prompt, ChessMove.class).get();
                moves.add(move);

                // Update boardState according to move.move
                // ...

                var boardPrompt = String.format("""
                            Given the following chess moves: %s,
                            generate a chess board on a table with appropriate emoji representations for each move and piece.
                            Add a brief description of the move and it's implications""",
                    moves.stream().map(it -> it.player + ":" + it.move).collect(Collectors.joining(", ")));

                ChessBoard chessBoard= scope.prompt(boardPrompt, ChessBoard.class).get();
                System.out.println("Current board:\n" + chessBoard.board);

                var gameStatePrompt = String.format("""
                            Given the following chess moves: %s,
                            has the game ended (win, draw, or stalemate)?""",
                      moves.stream().map(ChessMove::toString).collect(Collectors.joining(", ")));

                GameState gameState  = scope.prompt(gameStatePrompt, GameState.class).get();

                gameEnded = gameState.ended;
                winner = gameState.winner;
            }

            System.out.println("Game over. Final move: " + moves.get(moves.size() - 1) + ", Winner: " + winner);
        }
    }
}
