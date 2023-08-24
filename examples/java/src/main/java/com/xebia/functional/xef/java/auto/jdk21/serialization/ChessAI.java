package com.xebia.functional.xef.java.auto.jdk21.serialization;

import com.xebia.functional.xef.conversation.PlatformConversation;
import com.xebia.functional.xef.conversation.llm.openai.OpenAI;
import com.xebia.functional.xef.prompt.Prompt;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ChessAI {

    public record ChessMove(String player, String move){}
    public record ChessBoard(String board){}
    public record GameState(Boolean ended, String winner){}

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (PlatformConversation scope = OpenAI.conversation()) {
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

                ChessMove move = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt(prompt), ChessMove.class).get();
                moves.add(move);

                // Update boardState according to move.move
                // ...

                var boardPrompt = String.format("""
                            Given the following chess moves: %s,
                            generate a chess board on a table with appropriate emoji representations for each move and piece.
                            Add a brief description of the move and it's implications""",
                    moves.stream().map(it -> it.player + ":" + it.move).collect(Collectors.joining(", ")));

                ChessBoard chessBoard= scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt(boardPrompt), ChessBoard.class).get();
                System.out.println("Current board:\n" + chessBoard.board);

                var gameStatePrompt = String.format("""
                            Given the following chess moves: %s,
                            has the game ended (win, draw, or stalemate)?""",
                      moves.stream().map(ChessMove::toString).collect(Collectors.joining(", ")));

                GameState gameState  = scope.prompt(OpenAI.FromEnvironment.DEFAULT_SERIALIZATION, new Prompt(gameStatePrompt), GameState.class).get();

                gameEnded = gameState.ended;
                winner = gameState.winner;
            }

            System.out.println("Game over. Final move: " + moves.get(moves.size() - 1) + ", Winner: " + winner);
        }
    }
}
