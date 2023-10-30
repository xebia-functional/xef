package com.xebia.functional.xef.examples.scala.iteration

import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.scala.conversation.*
import com.xebia.functional.xef.scala.serialization.*
import io.circe.Decoder

import scala.annotation.tailrec

case class ChessMove(player: String, move: String) derives SerialDescriptor, Decoder
case class ChessBoard(board: String) derives SerialDescriptor, Decoder
case class GameState(ended: Boolean = false, winner: Option[String] = None) derives SerialDescriptor, Decoder

@tailrec
private def chessGame(moves: List[ChessMove] = Nil, gameState: GameState = new GameState)(using ScalaConversation): (String, ChessMove) =
  if !gameState.ended then
    val currentPlayer = if moves.size % 2 == 0 then "Player 1 (White)" else "Player 2 (Black)"
    val previousMoves = moves.map(m => m.player + ":" + m.move).mkString(", ")
    val movePrompt = moves match {
      case Nil => s"""
        |$currentPlayer, you are playing chess and it's your turn.
        |Make your first move:
      """.stripMargin
      case l => s"""
        |$currentPlayer, you are playing chess and it's your turn.
        |Here are the previous moves: $previousMoves
        |Make your next move:
      """.stripMargin
    }
    println(movePrompt)
    val move = prompt[ChessMove](movePrompt)
    println(s"Move is: $move")
    val boardPrompt =
      s"""
         |Given the following chess moves: $previousMoves,
         |generate a chess board on a table with appropriate emoji representations for each move and piece.
         |Add a brief description of the move and its implications.
      """.stripMargin
    val chessBoard = prompt[ChessBoard](boardPrompt)
    println(s"Current board:\n${chessBoard.board}")
    val gameStatePrompt =
      s"""
         |Given the following chess moves: ${moves.mkString(", ")},
         |has the game ended (win, draw, or stalemate)?
      """.stripMargin
    val gameState = prompt[GameState](gameStatePrompt)
    chessGame(moves :+ move, gameState)
  else (gameState.winner.getOrElse("Something went wrong"), moves.last)

@main def runChessGame(): Unit = conversation:
  val (winner, fMove) = chessGame()
  println(s"Game over. Final move: $fMove, Winner: $winner")
