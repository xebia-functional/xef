package com.xebia.functional.langchain4k.auto

import arrow.core.getOrElse
import com.xebia.functional.auto.ai
import kotlinx.serialization.Serializable

@Serializable
data class ChessMove(val player : String, val move: String)
@Serializable
data class ChessBoard(val board: String)
@Serializable
data class GameState(val ended: Boolean, val winner: String)

suspend fun main() {
    ai {
      val moves = mutableListOf<ChessMove>()
      var gameEnded = false
      var winner = ""

      while (!gameEnded) {
        val currentPlayer = if (moves.size % 2 == 0) "Player 1 (White)" else "Player 2 (Black)"
        val prompt = """
              |$currentPlayer, it's your turn. 
              |Previous moves: ${moves.joinToString(", ")}
              |Make your next move:
          """.trimIndent()

        val move: ChessMove = ai(prompt)
        moves.add(move)

        // Update boardState according to move.move
        // ...

        val boardPrompt = """
              Given the following chess moves: ${moves.joinToString(", ") { it.player + ":" + it.move }}},
              generate a chess board on a table with appropriate emoji representations for each move and piece.
              Add a brief description of the move and it's implications
          """.trimIndent()

        val chessBoard: ChessBoard = ai(boardPrompt)
        println("Current board:\n${chessBoard.board}")

        val gameStatePrompt = """
              Given the following chess moves: ${moves.joinToString(", ")},
              has the game ended (win, draw, or stalemate)?
          """.trimIndent()

        val gameState: GameState = ai(gameStatePrompt)

        gameEnded = gameState.ended
        winner = gameState.winner
      }

      println("Game over. Final move: ${moves.last()}, Winner: $winner")
    }.getOrElse { println(it) }
}

