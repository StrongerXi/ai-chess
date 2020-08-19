package aiChess.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import java.util.Optional;
import java.util.HashSet;

/**
 * Tests for the BoardModel class.
 */
public class BoardModelTest {

  @Test
  public void testEquality() {
    // 1 _ _ B
    // 0 _ c _
    //   0 1 2
    var bishop = Optional.of(PieceFactory.makePiece(PieceType.BISHOP, PlayerType.TOP_PLAYER));
    var castle = Optional.of(PieceFactory.makePiece(PieceType.CASTLE, PlayerType.BOTTOM_PLAYER));
    var board = new BoardModel(2, 3);
    board.setPieceAt(0, 1, castle);
    board.setPieceAt(1, 2, bishop);

    var bishopDup = Optional.of(PieceFactory.makePiece(PieceType.BISHOP, PlayerType.TOP_PLAYER));
    var castleDup = Optional.of(PieceFactory.makePiece(PieceType.CASTLE, PlayerType.BOTTOM_PLAYER));
    var boardDup = new BoardModel(2, 3);
    boardDup.setPieceAt(0, 1, castleDup);
    boardDup.setPieceAt(1, 2, bishopDup);

    assertEquals("Same board constructed separated", board, boardDup);
    assertEquals("Board copy equals", board.getCopy(), board);
    assertEquals("Board copy reference equality", false, board.getCopy() == board);
  }

  @Test
  public void testHashCode() {
    // 2 _ _ _
    // 1 _ _ B
    // 0 _ c _
    //   0 1 2
    var bishop = Optional.of(PieceFactory.makePiece(PieceType.BISHOP, PlayerType.TOP_PLAYER));
    var castle = Optional.of(PieceFactory.makePiece(PieceType.CASTLE, PlayerType.BOTTOM_PLAYER));
    var board = new BoardModel(3, 3);
    board.setPieceAt(0, 1, castle);
    board.setPieceAt(1, 2, bishop);

    var bishopDup = Optional.of(PieceFactory.makePiece(PieceType.BISHOP, PlayerType.TOP_PLAYER));
    var castleDup = Optional.of(PieceFactory.makePiece(PieceType.CASTLE, PlayerType.BOTTOM_PLAYER));
    var boardDup = new BoardModel(3, 3);
    boardDup.setPieceAt(0, 1, castleDup);
    boardDup.setPieceAt(1, 2, bishopDup);

    assertEquals("Same board constructed separated", board.hashCode(), boardDup.hashCode());
    assertEquals("Board copy equals", board.getCopy().hashCode(), board.hashCode());
  }

  @Test
  public void testGetAllLegalMoves1() {
    // 1 _ _ B
    // 0 _ c _
    //   0 1 2
    var bishop = Optional.of(PieceFactory.makePiece(PieceType.BISHOP, PlayerType.TOP_PLAYER));
    var castle = Optional.of(PieceFactory.makePiece(PieceType.CASTLE, PlayerType.BOTTOM_PLAYER));
    var board = new BoardModel(2, 3);
    board.setPieceAt(0, 1, castle);
    board.setPieceAt(1, 2, bishop);
    assertEquals("empty", Optional.empty(), board.getPieceAt(0, 0));
    assertEquals("initialized bishop", castle, board.getPieceAt(0, 1));
    assertEquals("empty", Optional.empty(), board.getPieceAt(0, 2));
    assertEquals("empty", Optional.empty(), board.getPieceAt(1, 0));
    assertEquals("empty", Optional.empty(), board.getPieceAt(1, 1));
    assertEquals("initialized castle", bishop, board.getPieceAt(1, 2));

    // 1  _ _ _
    // 0  B _ c
    //    0 1 2
    board.setPieceAt(0, 1, Optional.empty());
    board.setPieceAt(1, 2, Optional.empty());
    board.setPieceAt(0, 2, castle);
    board.setPieceAt(0, 0, bishop);
    assertEquals("moved castle", bishop, board.getPieceAt(0, 0));
    assertEquals("empty", Optional.empty(), board.getPieceAt(0, 1));
    assertEquals("moved bishop", castle, board.getPieceAt(0, 2));
    assertEquals("empty", Optional.empty(), board.getPieceAt(1, 0));
    assertEquals("empty", Optional.empty(), board.getPieceAt(1, 1));
    assertEquals("empty", Optional.empty(), board.getPieceAt(1, 2));
  }

  @Test
  public void testIsMoveLegal1() {
    // 2 _ K _
    // 1 _ _ _
    // 0 _ k _
    //   0 1 2
    var board = new BoardModel(3, 3);
    var topKing = Optional.of(PieceFactory.makePiece(PieceType.KING, PlayerType.TOP_PLAYER));
    var botKing = Optional.of(PieceFactory.makePiece(PieceType.KING, PlayerType.BOTTOM_PLAYER));
    board.setPieceAt(2, 1, topKing);
    board.setPieceAt(0, 1, botKing);
    var topKingPos = Position.of(2, 1);
    var botKingPos = Position.of(0, 1);
    var topLegalMoves = new HashSet<Move>();
    var botLegalMoves = new HashSet<Move>();

    for (int row = 0; row < 3; row += 1) {
      for (int col = 0; col < 3; col += 1) {
        // neither king can move to the middle row (row == 1)
        var topLegal = row == 2;
        var botLegal = row == 0;
        var targetPos = Position.of(row, col);
        // make sure the moves are pseudo-legal.
        if (row == 2 && Math.abs(1 - col) == 1) {
          topLegalMoves.add(MoveFactory.makeRegularMove(topKingPos, targetPos));
        }
        if (row == 0 && Math.abs(1 - col) == 1) {
          botLegalMoves.add(MoveFactory.makeRegularMove(botKingPos, targetPos));
        }
      }
    }
    var topReturnedMoves = board.getAllLegalMoves(PlayerType.TOP_PLAYER);
    assertEquals("check top king moves", topLegalMoves, new HashSet<>(topReturnedMoves));
    assertEquals("top king moves have duplicates", topLegalMoves.size(), topReturnedMoves.size());
    var botReturnedMoves = board.getAllLegalMoves(PlayerType.BOTTOM_PLAYER);
    assertEquals("check bottom king moves", botLegalMoves, new HashSet<>(botReturnedMoves));
    assertEquals("bottom king moves have duplicates", botLegalMoves.size(), botReturnedMoves.size());
  }

  @Test
  public void testGetAllLegalMoves2() {
    // 5 K _ _ _ q _
    // 4 _ B _ _ _ _
    // 3 _ _ _ _ C _
    // 2 _ _ _ p _ _
    // 1 Q _ _ n k _
    // 0 c _ _ _ _ _
    //   0 1 2 3 4 5
    // Both kings are in check, so both sides' legal moves are restricted.
    var board = new BoardModel(6, 6);

    var topKing = Optional.of(PieceFactory.makePiece(PieceType.KING, PlayerType.TOP_PLAYER));
    var topCastle = Optional.of(PieceFactory.makePiece(PieceType.CASTLE, PlayerType.TOP_PLAYER));
    var topQueen = Optional.of(PieceFactory.makePiece(PieceType.QUEEN, PlayerType.TOP_PLAYER));
    var topBishop = Optional.of(PieceFactory.makePiece(PieceType.BISHOP, PlayerType.TOP_PLAYER));

    var botKing = Optional.of(PieceFactory.makePiece(PieceType.KING, PlayerType.BOTTOM_PLAYER));
    var botCastle = Optional.of(PieceFactory.makePiece(PieceType.CASTLE, PlayerType.BOTTOM_PLAYER));
    var botQueen = Optional.of(PieceFactory.makePiece(PieceType.QUEEN, PlayerType.BOTTOM_PLAYER));
    var botPawn = Optional.of(PieceFactory.makePiece(PieceType.PAWN, PlayerType.BOTTOM_PLAYER));
    var botKnight = Optional.of(PieceFactory.makePiece(PieceType.KNIGHT, PlayerType.BOTTOM_PLAYER));

    var topKingPos   = Position.of(5, 0);
    var topCastlePos = Position.of(3, 4);
    var topQueenPos  = Position.of(1, 0);
    var topBishopPos = Position.of(4, 1);

    var botKingPos   = Position.of(1, 4);
    var botCastlePos = Position.of(0, 0);
    var botQueenPos  = Position.of(5, 4);
    var botPawnPos   = Position.of(2, 3);
    var botKnightPos = Position.of(1, 3);

    board.setPieceAt(5, 0, topKing);
    board.setPieceAt(3, 4, topCastle);
    board.setPieceAt(1, 0, topQueen);
    board.setPieceAt(4, 1, topBishop);

    board.setPieceAt(1, 4, botKing);
    board.setPieceAt(0, 0, botCastle);
    board.setPieceAt(5, 4, botQueen);
    board.setPieceAt(2, 3, botPawn);
    board.setPieceAt(1, 3, botKnight);

    var topLegalMoves = new HashSet<Move>();
    var botLegalMoves = new HashSet<Move>();

    // top king escapes queen attack
    topLegalMoves.add(MoveFactory.makeRegularMove(topKingPos, Position.of(4, 0)));
    // top castle, save king or capture opponent king
    topLegalMoves.add(MoveFactory.makeRegularMove(topCastlePos, botQueenPos));
    topLegalMoves.add(MoveFactory.makeRegularMove(topCastlePos, botKingPos));
    // top queen can't save or capture
    // top bishop can block queen to save king
    topLegalMoves.add(MoveFactory.makeRegularMove(topBishopPos, Position.of(5, 2)));


    // bottom king escape castle attack (to left or right)
    botLegalMoves.add(MoveFactory.makeRegularMove(botKingPos, Position.of(0, 3)));
    botLegalMoves.add(MoveFactory.makeRegularMove(botKingPos, Position.of(2, 5)));
    botLegalMoves.add(MoveFactory.makeRegularMove(botKingPos, Position.of(1, 5)));
    botLegalMoves.add(MoveFactory.makeRegularMove(botKingPos, Position.of(0, 5)));
    // bottom castle can't save or capture
    // bottom queen can capture castle to save king or capture opponent King
    botLegalMoves.add(MoveFactory.makeRegularMove(botQueenPos, topCastlePos));
    botLegalMoves.add(MoveFactory.makeRegularMove(botQueenPos, topKingPos));
    // bottom pawn can't capture castle to save king, because that exposes king to bishop
    // bottom knight can't capture castle to save king, because that exposes king to queen

    var topReturnedMoves = board.getAllLegalMoves(PlayerType.TOP_PLAYER);
    assertEquals("check top player moves", topLegalMoves, new HashSet<>(topReturnedMoves));
    assertEquals("top king moves have duplicates", topLegalMoves.size(), topReturnedMoves.size());

    var botReturnedMoves = board.getAllLegalMoves(PlayerType.BOTTOM_PLAYER);
    assertEquals("check bottom player moves", botLegalMoves, new HashSet<>(botReturnedMoves));
    assertEquals("bottom king moves have duplicates", botLegalMoves.size(), botReturnedMoves.size());
  }

  @Test
  public void testGetAllLegalMovesCastling() {
    // 5 C _ K _ _ C _
    // 4 _ _ _ _ _ _ _
    // 3 _ _ _ _ _ _ _
    // 2 _ _ _ _ _ _ _
    // 1 _ q _ _ _ _ _
    // 0 c _ k _ n c _
    //   0 1 2 3 4 5 6
    // Top king can only do right-castling, since (5, 1) is attacked by bottom queen.
    // Bottom king can only do left-castling, since (0, 4) is occupied.
    var board = new BoardModel(7, 6);

    var topKing = Optional.of(PieceFactory.makePiece(PieceType.KING, PlayerType.TOP_PLAYER));
    var topCastle = Optional.of(PieceFactory.makePiece(PieceType.CASTLE, PlayerType.TOP_PLAYER));

    var botKing = Optional.of(PieceFactory.makePiece(PieceType.KING, PlayerType.BOTTOM_PLAYER));
    var botCastle = Optional.of(PieceFactory.makePiece(PieceType.CASTLE, PlayerType.BOTTOM_PLAYER));
    var botQueen = Optional.of(PieceFactory.makePiece(PieceType.QUEEN, PlayerType.BOTTOM_PLAYER));
    var botKnight = Optional.of(PieceFactory.makePiece(PieceType.KNIGHT, PlayerType.BOTTOM_PLAYER));

    var topKingPos        = Position.of(5, 2);
    var botKingPos        = Position.of(0, 2);

    board.setPieceAt(5, 2, topKing);
    board.setPieceAt(5, 0, topCastle);
    board.setPieceAt(5, 5, topCastle);

    board.setPieceAt(0, 2, botKing);
    board.setPieceAt(0, 0, botCastle);
    board.setPieceAt(0, 5, botCastle);
    board.setPieceAt(1, 1, botQueen);
    board.setPieceAt(0, 4, botKnight);

    var topLegalMoves = board.getAllLegalMoves(PlayerType.TOP_PLAYER);
    var botLegalMoves = board.getAllLegalMoves(PlayerType.BOTTOM_PLAYER);

    var topRightCastling = MoveFactory.makeCastling(topKingPos, Position.of(5, 4));
    var topLeftCastling  = MoveFactory.makeCastling(topKingPos, Position.of(5, 1));
    var botRightCastling = MoveFactory.makeCastling(botKingPos, Position.of(0, 4));
    var botLeftCastling  = MoveFactory.makeCastling(botKingPos, Position.of(0, 1));

    assertTrue("top legal moves includes right castling", topLegalMoves.contains(topRightCastling));
    assertFalse("top legal moves excludes left castling", topLegalMoves.contains(topLeftCastling));
    assertFalse("bot legal moves includes right castling", botLegalMoves.contains(botRightCastling));
    assertTrue("bot legal moves excludes left castling", botLegalMoves.contains(botLeftCastling));
  }

  @Test
  public void testGetAllLegalMovesPawnPromotion() {
    // 5 _ _ _ _ _ K
    // 4 _ _ p _ _ _
    // 3 _ _ _ _ _ _
    // 2 _ _ _ _ _ _
    // 1 _ P _ _ B _
    // 0 c _ q k _ _
    //   0 1 2 3 4 5
    // Promote by forward move or diagonal capturing.
    var board = new BoardModel(6, 6);

    var topKing   = Optional.of(PieceFactory.makePiece(PieceType.KING, PlayerType.TOP_PLAYER));
    var topPawn   = Optional.of(PieceFactory.makePiece(PieceType.PAWN, PlayerType.TOP_PLAYER));
    var topBishop = Optional.of(PieceFactory.makePiece(PieceType.BISHOP, PlayerType.TOP_PLAYER));

    var botKing   = Optional.of(PieceFactory.makePiece(PieceType.KING, PlayerType.BOTTOM_PLAYER));
    var botPawn   = Optional.of(PieceFactory.makePiece(PieceType.PAWN, PlayerType.BOTTOM_PLAYER));
    var botCastle = Optional.of(PieceFactory.makePiece(PieceType.CASTLE, PlayerType.BOTTOM_PLAYER));
    var botQueen  = Optional.of(PieceFactory.makePiece(PieceType.QUEEN, PlayerType.BOTTOM_PLAYER));

    var topPawnPos = Position.of(1, 1);
    var botPawnPos = Position.of(4, 2);

    board.setPieceAt(5, 5, topKing);
    board.setPieceAt(1, 1, topPawn);
    board.setPieceAt(1, 4, topBishop);

    board.setPieceAt(4, 2, botPawn);
    board.setPieceAt(0, 3, botKing);
    board.setPieceAt(0, 0, botCastle);
    board.setPieceAt(0, 2, botQueen);

    var topLegalMoves = board.getAllLegalMoves(PlayerType.TOP_PLAYER);
    var botLegalMoves = board.getAllLegalMoves(PlayerType.BOTTOM_PLAYER);

    var topPawnPromotionLeft  = MoveFactory.makePawnPromotion(topPawnPos, Position.of(0, 0));
    var topPawnPromotionMid   = MoveFactory.makePawnPromotion(topPawnPos, Position.of(0, 1));
    var topPawnPromotionRight = MoveFactory.makePawnPromotion(topPawnPos, Position.of(0, 2));
    var botPawnPromotion      = MoveFactory.makePawnPromotion(botPawnPos, Position.of(5, 2));

    assertTrue("top legal moves includes left pawn promotion", topLegalMoves.contains(topPawnPromotionLeft));
    assertTrue("top legal moves includes middle pawn promotion", topLegalMoves.contains(topPawnPromotionMid));
    assertTrue("top legal moves includes right pawn promotion", topLegalMoves.contains(topPawnPromotionRight));
    assertFalse("bot legal moves excludes forward pawn promotion", botLegalMoves.contains(botPawnPromotion));
  }
}
