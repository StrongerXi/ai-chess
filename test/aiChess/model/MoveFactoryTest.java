package aiChess.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;
import org.junit.Before;

import java.util.Optional;


/**
 * Tests for the Move instances obtained from MoveFactory class, 
 * specifically the apply/undo logic.
 */
public class MoveFactoryTest {

  /* a 8x8 board to fascilitate tests */
  private BoardModel board;
  @Before
  public void initBoard() {
    this.board = new BoardModel(8, 8);
  }


  /* Test move logic for different pieces 
   * NOTE:
   * - the move logic does not care about the specific type of piece
   * - top player's pieces will be written as uppercase in comments. */ 

  /* test move with source == target */
  @Test
  public void testSelfMove() {
    // _ K
    // _ _ 
    var pos11 = new Position(1, 1); 
    var king = Optional.of(PieceFactory.makePiece(PieceType.KING, PlayerType.TOP_PLAYER));
    var kingMoved = Optional.of(PieceFactory.makePieceMoved(PieceType.KING, PlayerType.TOP_PLAYER, true));
    var move = MoveFactory.makeRegularMove(pos11, pos11);
    this.board.setPieceAt(1, 1, king);

    move.apply(this.board);
    for (int row = 0; row < board.height; row += 1) {
      for (int col = 0; col < board.width; col += 1) {
        var pos = new Position(row, col);
        if (row == 1 && col == 1) {
          assertEquals("king tile at " + pos, kingMoved, board.getPieceAt(row, col));
        } else {
          assertEquals("empty tile at " + pos, Optional.empty(), board.getPieceAt(row, col));
        }
      }
    }

    move.undo(this.board);
    for (int row = 0; row < board.height; row += 1) {
      for (int col = 0; col < board.width; col += 1) {
        var pos = new Position(row, col);
        if (row == 1 && col == 1) {
          assertEquals("king tile at " + pos, king, board.getPieceAt(row, col));
        } else {
          assertEquals("empty tile at " + pos, Optional.empty(), board.getPieceAt(row, col));
        }
      }
    }
  }


  /* test apply and undo methods of regular move */
  @Test
  public void testRegularMove() {
    /* _ C _ _
     * _ _ B _
     * _ p _ _
     * _ _ _ K
     */
    Position pos11 = new Position(1, 1); // bottom pawn
    Position pos22 = new Position(2, 2); // top bishop
    Position pos31 = new Position(3, 1); // top castle
    Position pos03 = new Position(0, 3); // top knight
    Position pos33 = new Position(3, 3);
    Position pos12 = new Position(1, 2);

    /* regualr moves without attacking */
    Move castleRight = MoveFactory.makeRegularMove(pos31, pos33);
    Move bishopDown = MoveFactory.makeRegularMove(pos22, pos12);
    /* moves with attacking (piece consumption) */
    Move castleAttack = MoveFactory.makeRegularMove(pos31, pos11); // top castle attack bottom pawn
    Move pawnAttack = MoveFactory.makeRegularMove(pos11, pos22); // bottom pawn attack top bishop 
    Move bishopAttack = MoveFactory.makeRegularMove(pos22, pos11); // top bishop attack bottom pawn
    Move knightAttack = MoveFactory.makeRegularMove(pos03, pos11); // top knight attack bottom pawn

    Piece bottomPawn = PieceFactory.makePiece(PieceType.PAWN, PlayerType.BOTTOM_PLAYER);
    Piece topBishop = PieceFactory.makePiece(PieceType.BISHOP, PlayerType.TOP_PLAYER);
    Piece topCastle = PieceFactory.makePiece(PieceType.CASTLE, PlayerType.TOP_PLAYER);
    Piece topKnight = PieceFactory.makePiece(PieceType.KNIGHT, PlayerType.TOP_PLAYER);

    Piece bottomPawnMoved = PieceFactory.makePieceMoved(PieceType.PAWN, PlayerType.BOTTOM_PLAYER, true);
    Piece topBishopMoved = PieceFactory.makePieceMoved(PieceType.BISHOP, PlayerType.TOP_PLAYER, true);
    Piece topCastleMoved = PieceFactory.makePieceMoved(PieceType.CASTLE, PlayerType.TOP_PLAYER, true);
    Piece topKnightMoved = PieceFactory.makePieceMoved(PieceType.KNIGHT, PlayerType.TOP_PLAYER, true);

    this.board.setPieceAt(1, 1, Optional.of(bottomPawn));
    this.board.setPieceAt(2, 2, Optional.of(topBishop));
    this.board.setPieceAt(3, 1, Optional.of(topCastle));
    this.board.setPieceAt(0, 3, Optional.of(topKnight));

    /* apply move, query board state, undo move, query board state */
    castleRight.apply(this.board);
    assertEquals("move source empty\n", Optional.empty(), board.getPieceAt(3, 1));
    assertEquals("move target becomes castle\n", Optional.of(topCastleMoved), board.getPieceAt(3, 3));
    castleRight.undo(this.board);
    assertEquals("move source back to castle\n", Optional.of(topCastle), board.getPieceAt(3, 1));
    assertEquals("move target back to empty\n", Optional.empty(), board.getPieceAt(3, 3));

    bishopDown.apply(this.board);
    assertEquals("move source empty\n", Optional.empty(), board.getPieceAt(2, 2));
    assertEquals("move target becomes pawn\n", Optional.of(topBishopMoved), board.getPieceAt(1, 2));
    bishopDown.undo(this.board);
    assertEquals("move source back to pawn\n", Optional.of(topBishop), board.getPieceAt(2, 2));
    assertEquals("move target back to empty\n", Optional.empty(), board.getPieceAt(1, 2));

    /* do the same to move with attack */

    castleAttack.apply(this.board);
    assertEquals("move source empty\n", Optional.empty(), board.getPieceAt(3, 1));
    assertEquals("move target becomes castle\n", Optional.of(topCastleMoved), board.getPieceAt(1, 1));
    castleAttack.undo(this.board);
    assertEquals("move source back to castle\n", Optional.of(topCastle), board.getPieceAt(3, 1));
    assertEquals("move target back to pawn\n", Optional.of(bottomPawn), board.getPieceAt(1, 1));

    pawnAttack.apply(this.board);
    assertEquals("move source empty\n", Optional.empty(), board.getPieceAt(1, 1));
    assertEquals("move target becomes pawn\n", Optional.of(bottomPawnMoved), board.getPieceAt(2, 2));
    pawnAttack.undo(this.board);
    assertEquals("move source back to pawn\n", Optional.of(bottomPawn), board.getPieceAt(1, 1));
    assertEquals("move target back to bishop\n", Optional.of(topBishop), board.getPieceAt(2, 2));

    bishopAttack.apply(this.board);
    assertEquals("move source empty\n", Optional.empty(), board.getPieceAt(2, 2));
    assertEquals("move target becomes bishop\n", Optional.of(topBishopMoved), board.getPieceAt(1, 1));
    bishopAttack.undo(this.board);
    assertEquals("move source back to bishop\n", Optional.of(topBishop), board.getPieceAt(2, 2));
    assertEquals("move target back to pawn\n", Optional.of(bottomPawn), board.getPieceAt(1, 1));


    knightAttack.apply(this.board);
    assertEquals("move source empty\n", Optional.empty(), board.getPieceAt(0, 3));
    assertEquals("move target becomes knight\n", Optional.of(topKnightMoved), board.getPieceAt(1, 1));
    knightAttack.undo(this.board);
    assertEquals("move source back to knight\n", Optional.of(topKnight), board.getPieceAt(0, 3));
    assertEquals("move target back to pawn\n", Optional.of(bottomPawn), board.getPieceAt(1, 1));
  }


  /* test apply and undo methods of castling move
   * NOTE: it's _not_ about the move generation. */
  @Test
  public void testCastlingMove() {
    /* 5 C _ K Q _ _
     * 4 P P P P P P
     * 3 _ _ _ _ _ _
     * 2 _ _ _ _ _ _
     * 1 p p p p p p
     * 0 n k _ _ c n
     *   0 1 2 3 4 5
     */
    Move topCastling = MoveFactory.makeCastling(new Position(5, 2), new Position(5, 1));
    Move botCastling = MoveFactory.makeCastling(new Position(0, 1), new Position(0, 3));

    var topPawn   = Optional.of(PieceFactory.makePiece(PieceType.PAWN,   PlayerType.TOP_PLAYER));
    var topKing   = Optional.of(PieceFactory.makePiece(PieceType.KING,   PlayerType.TOP_PLAYER));
    var topQueen  = Optional.of(PieceFactory.makePiece(PieceType.QUEEN,  PlayerType.TOP_PLAYER));
    var topCastle = Optional.of(PieceFactory.makePiece(PieceType.CASTLE, PlayerType.TOP_PLAYER));
    var botPawn   = Optional.of(PieceFactory.makePiece(PieceType.PAWN,   PlayerType.BOTTOM_PLAYER));
    var botKing   = Optional.of(PieceFactory.makePiece(PieceType.KING,   PlayerType.BOTTOM_PLAYER));
    var botKnight = Optional.of(PieceFactory.makePiece(PieceType.KNIGHT, PlayerType.BOTTOM_PLAYER));
    var botCastle = Optional.of(PieceFactory.makePiece(PieceType.CASTLE, PlayerType.BOTTOM_PLAYER));

    this.board.setPieceAt(5, 0, topCastle);
    this.board.setPieceAt(5, 2, topKing);
    this.board.setPieceAt(5, 3, topQueen);
    this.board.setPieceAt(0, 0, botKnight);
    this.board.setPieceAt(0, 1, botKing);
    this.board.setPieceAt(0, 4, botCastle);
    this.board.setPieceAt(0, 5, botKnight);
    for (int col = 0; col < 6; col += 1) {
      this.board.setPieceAt(4, col, topPawn);
      this.board.setPieceAt(1, col, botPawn);
    }

    // Castling move apply/undo shouldn't affect other pieces
    // So we check the whole board

    /* apply move, query board state, undo move, query board state
     * 5 _ K C Q _ _
     * 4 P P P P P P
     * 3 _ _ _ _ _ _
     * 2 _ _ _ _ _ _
     * 1 p p p p p p
     * 0 n k _ _ c n
     *   0 1 2 3 4 5
     */
    var boardCopy  = this.board.getCopy();
    var boardMoved = this.board.getCopy();
    boardMoved.setPieceAt(5, 0, Optional.empty());
    boardMoved.setPieceAt(5, 1, topKing.map(p -> p.setMoved(true)));
    boardMoved.setPieceAt(5, 2, topCastle.map(p -> p.setMoved(true)));

    topCastling.apply(this.board);
    assertEquals("board after apply top castling\n", boardMoved, this.board);
    topCastling.undo(this.board);
    assertEquals("board after undo top castling\n", boardCopy, this.board);

    /* apply move, query board state, undo move, query board state
     * 5 C _ K Q _ _
     * 4 P P P P P P
     * 3 _ _ _ _ _ _
     * 2 _ _ _ _ _ _
     * 1 p p p p p p
     * 0 n _ c k _ n
     *   0 1 2 3 4 5
     */
    boardMoved = this.board.getCopy();
    boardMoved.setPieceAt(0, 1, Optional.empty());
    boardMoved.setPieceAt(0, 2, botCastle.map(p -> p.setMoved(true)));
    boardMoved.setPieceAt(0, 3, botKing.map(p -> p.setMoved(true)));
    boardMoved.setPieceAt(0, 4, Optional.empty());

    botCastling.apply(this.board);
    assertEquals("board after apply bottom castling\n", boardMoved, this.board);
    botCastling.undo(this.board);
    assertEquals("board after undo bottom castling\n", boardCopy, this.board);
  }
}
