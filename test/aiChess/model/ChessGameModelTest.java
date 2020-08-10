package aiChess.model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Tests for the ChessGameModel class.
 */
public class ChessGameModelTest {


  /* Test `getAllMovesFrom` for an initial 8x8 chess board */
  @Test
  public void testGetAllMovesFromInitial() {
    // 7 C N B Q K B N C 
    // 6 P P P P P P P P 
    // 5 _ _ _ _ _ _ _ _ 
    // 4 _ _ _ _ _ _ _ _ 
    // 3 _ _ _ _ _ _ _ _ 
    // 2 _ _ _ _ _ _ _ _ 
    // 1 p p p p p p p p 
    // 0 c n b q k b n c 
    //   0 1 2 3 4 5 6 7

    var game = new ChessGameModel();
    // empty tiles
    for (int row = 2; row < 6; row += 1) {
      for (int col = 0; col < 8; col += 1) {
        var actualMoves = new ArrayList<Position>(game.getAllMovesFrom(row, col));
        var expectMoves = new ArrayList<Position>();
        assertEquals("Empty tile has no legal moves", expectMoves, actualMoves);
      }
    }

    // top player pieces
    for (int row = 6; row < 8; row += 1) {
      for (int col = 0; col < 8; col += 1) {
        var actualMoves = new ArrayList<Position>(game.getAllMovesFrom(row, col));
        var expectMoves = new ArrayList<Position>();
        assertEquals("Not top player's turn, no legal moves", expectMoves, actualMoves);
      }
    }

    // bottom pawns
    for (int col = 0; col < 8; col += 1) {
      var actualMoves = game.getAllMovesFrom(1, col);
      var expectMoves = new HashSet<Position>();
      expectMoves.add(new Position(2, col));
      expectMoves.add(new Position(3, col));
      assertEquals("Check bottom pawn moves at column " + col, expectMoves, new HashSet<>(actualMoves));
      assertEquals("Check duplicates in bottom pawn move at column " + col, expectMoves.size(), actualMoves.size());
    }

    // only knights can move in the border row
    for (int col = 0; col < 8; col += 1) {
      var actualMoves = game.getAllMovesFrom(0, col);
      if (col != 1 && col != 6) {
        var expectMoves = new ArrayList<Position>();
        assertEquals("No legal move for non-knight at " + col, expectMoves, new ArrayList<>(actualMoves));

      } else {
        var expectMoves = new HashSet<Position>();
        expectMoves.add(new Position(2, col - 1));
        expectMoves.add(new Position(2, col + 1));
        assertEquals("Check bottom knight moves at column " + col, expectMoves, new HashSet<>(actualMoves));
        assertEquals("Check duplicates in bottom knight move at column " + col, expectMoves.size(), actualMoves.size());
      }
    }
  }


  /* Test `getAllMovesFrom` for a board with king in check */
  @Test
  public void testGetAllMovesFromInCheck() {
    // 5 _ _ _ _ _ _ 
    // 4 _ P _ _ K _ 
    // 3 p _ q _ N _ 
    // 2 _ B _ c _ _ 
    // 1 _ _ k _ _ _ 
    // 0 _ _ _ _ _ _ 
    //   0 1 2 3 4 5 

    var board = new BoardModel(6, 6);

    var topKing = Optional.of(PieceFactory.makePiece(PieceType.KING, PlayerType.TOP_PLAYER));
    var topPawn = Optional.of(PieceFactory.makePiece(PieceType.PAWN, PlayerType.TOP_PLAYER));
    var topKnight = Optional.of(PieceFactory.makePiece(PieceType.KNIGHT, PlayerType.TOP_PLAYER));
    var topBishop = Optional.of(PieceFactory.makePiece(PieceType.BISHOP, PlayerType.TOP_PLAYER));

    var botKing = Optional.of(PieceFactory.makePiece(PieceType.KING, PlayerType.BOTTOM_PLAYER));
    var botCastle = Optional.of(PieceFactory.makePiece(PieceType.CASTLE, PlayerType.BOTTOM_PLAYER));
    var botQueen = Optional.of(PieceFactory.makePiece(PieceType.QUEEN, PlayerType.BOTTOM_PLAYER));
    var botPawn = Optional.of(PieceFactory.makePiece(PieceType.PAWN, PlayerType.BOTTOM_PLAYER));

    var topKingPos   = new Position(4, 4);
    var topPawnPos   = new Position(4, 1);
    var topKnightPos = new Position(3, 4);
    var topBishopPos = new Position(2, 1);

    var botKingPos   = new Position(1, 2);
    var botCastlePos = new Position(2, 3);
    var botQueenPos  = new Position(3, 2);
    var botPawnPos   = new Position(3, 0);

    board.setPieceAt(4, 4, topKing);
    board.setPieceAt(4, 1, topPawn);
    board.setPieceAt(3, 4, topKnight);
    board.setPieceAt(2, 1, topBishop);

    board.setPieceAt(1, 2, botKing);
    board.setPieceAt(2, 3, botCastle);
    board.setPieceAt(3, 2, botQueen);
    board.setPieceAt(3, 0, botPawn);

    var game = new ChessGameModel(board, PlayerType.BOTTOM_PLAYER);
    for (var pos : new Position[]{topKingPos, topPawnPos, topKnightPos, topBishopPos}) {
      var actualMoves = new ArrayList<Position>(game.getAllMovesFrom(pos.row, pos.col));
      var expectMoves = new ArrayList<Position>();
      assertEquals("Not top player's turn, no legal moves", expectMoves, actualMoves);
    }

    // bottom king needs to escape/capture bishop, and watch out for knight
    var botKingExpectMoves = new HashSet<Position>();
    botKingExpectMoves.add(new Position(0, 1));
    botKingExpectMoves.add(new Position(0, 2));
    botKingExpectMoves.add(new Position(1, 1));
    botKingExpectMoves.add(topBishopPos);
    var botKingActualMoves = game.getAllMovesFrom(1, 2);
    assertEquals("check bottom king moves", botKingExpectMoves, new HashSet<>(botKingActualMoves));
    assertEquals("check duplicates in bottom king moves", botKingExpectMoves.size(), botKingActualMoves.size());

    // bottom castle must capture bishop to save king
    var botCastleExpectMoves = new HashSet<Position>();
    botCastleExpectMoves.add(topBishopPos);
    var botCastleActualMoves = game.getAllMovesFrom(2, 3);
    assertEquals("check bottom castle moves", botCastleExpectMoves, new HashSet<>(botCastleActualMoves));
    assertEquals("check duplicates in bottom castle moves", botCastleExpectMoves.size(), botCastleActualMoves.size());

    // bottom queen must capture bishop to save king
    var botQueenExpectMoves = new HashSet<Position>();
    botQueenExpectMoves.add(topBishopPos);
    var botQueenActualMoves = game.getAllMovesFrom(3, 2);
    assertEquals("check bottom queen moves", botQueenExpectMoves, new HashSet<>(botQueenActualMoves));
    assertEquals("check duplicates in bottom queen moves", botQueenExpectMoves.size(), botQueenActualMoves.size());
    
    // bottom pawn can't move or capture, since king is in check
    var botPawnExpectMoves = new HashSet<Position>();
    var botPawnActualMoves = game.getAllMovesFrom(3, 0);
    assertEquals("check bottom pawn moves", botPawnExpectMoves, new HashSet<>(botPawnActualMoves));
    assertEquals("check duplicates in bottom pawn moves", botPawnExpectMoves.size(), botPawnActualMoves.size());
  }
}
