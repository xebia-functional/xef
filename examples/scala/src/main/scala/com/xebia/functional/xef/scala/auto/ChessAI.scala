package com.xebia.functional.xef.scala.auto

import com.xebia.functional.xef.scala.auto.*
import io.circe.Decoder

import scala.annotation.tailrec

private final case class ChessMove(player: String, move: String) derives SerialDescriptor, Decoder

private final case class ChessBoard(board: String) derives SerialDescriptor, Decoder

private final case class GameState(ended: Boolean, winner: Option[String]) derives SerialDescriptor, Decoder

@tailrec
private def chessGame(moves: List[ChessMove], gameState: GameState)(using conversation: ScalaConversation): (String, ChessMove) =
  if !gameState.ended then
    val currentPlayer = if moves.size % 2 == 0 then "Player 1 (White)" else "Player 2 (Black)"

    val previousMoves = moves.map(m => m.player + ":" + m.move).mkString(", ")

    val movePrompt = moves match {
      case Nil => s"""
                     |$currentPlayer, you are playing chess and it's your turn.
                     |These are no previous chess board moves.
                     |Make your first move:
      """.stripMargin
      case l => s"""
                   |$currentPlayer, you are playing chess and it's your turn.
                   |These are the previous chess board moves: $previousMoves
                   |Make your next move:
      """.stripMargin
    }
    println(movePrompt)
    val move: ChessMove = prompt(movePrompt)
    println(s"Move is: $move")

    val boardPrompt =
      s"""
         |Given the following chess moves: $previousMoves,
         |generate a chess board on a table with appropriate emoji representations for each move and piece.
         |Add a brief description of the move and it's implications
      """.stripMargin

    val chessBoard: ChessBoard = prompt(boardPrompt)
    println(s"Current board:\n${chessBoard.board}")

    val gameStatePrompt =
      s"""
         |Given the following chess moves: ${moves.mkString(", ")},
         |has the game ended (win, draw, or stalemate)?
      """.stripMargin

    val gameState: GameState = prompt(gameStatePrompt)

    chessGame(moves :+ move, gameState)
  else (gameState.winner.getOrElse("Something went wrong"), moves.last)

@main def runChess: Unit =
  conversation {
    val (winner, fMove) = chessGame(Nil, GameState(false, None))
    println(s"Game over. Final move: $fMove, Winner: $winner")
  }
