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

    this.board.setPieceAt(1, 1, Optional.of(bottomPawn));
    this.board.setPieceAt(2, 2, Optional.of(topBishop));
    this.board.setPieceAt(3, 1, Optional.of(topCastle));
    this.board.setPieceAt(0, 3, Optional.of(topKnight));

    /* apply move, query board state, undo move, query board state */
    castleRight.apply(this.board);
    assertEquals("move source empty\n", Optional.empty(), board.getPieceAt(3, 1));
    assertEquals("move target becomes castle\n", Optional.of(topCastle), board.getPieceAt(3, 3));
    assertEquals(true, topCastle.hasMoved());
    castleRight.undo(this.board);
    assertEquals("move source back to castle\n", Optional.of(topCastle), board.getPieceAt(3, 1));
    assertEquals("move target back to empty\n", Optional.empty(), board.getPieceAt(3, 3));
    assertEquals(false, topCastle.hasMoved());

    bishopDown.apply(this.board);
    assertEquals("move source empty\n", Optional.empty(), board.getPieceAt(2, 2));
    assertEquals("move target becomes pawn\n", Optional.of(topBishop), board.getPieceAt(1, 2));
    assertEquals(true, topBishop.hasMoved());
    bishopDown.undo(this.board);
    assertEquals("move source back to pawn\n", Optional.of(topBishop), board.getPieceAt(2, 2));
    assertEquals("move target back to empty\n", Optional.empty(), board.getPieceAt(1, 2));
    assertEquals(false, topBishop.hasMoved());

    /* do the same to move with attack */

    castleAttack.apply(this.board);
    assertEquals("move source empty\n", Optional.empty(), board.getPieceAt(3, 1));
    assertEquals("move target becomes castle\n", Optional.of(topCastle), board.getPieceAt(1, 1));
    assertEquals(true, topCastle.hasMoved());
    castleAttack.undo(this.board);
    assertEquals("move source back to castle\n", Optional.of(topCastle), board.getPieceAt(3, 1));
    assertEquals("move target back to pawn\n", Optional.of(bottomPawn), board.getPieceAt(1, 1));
    assertEquals(false, topCastle.hasMoved());

    pawnAttack.apply(this.board);
    assertEquals("move source empty\n", Optional.empty(), board.getPieceAt(1, 1));
    assertEquals("move target becomes pawn\n", Optional.of(bottomPawn), board.getPieceAt(2, 2));
    assertEquals(true, bottomPawn.hasMoved());
    pawnAttack.undo(this.board);
    assertEquals("move source back to pawn\n", Optional.of(bottomPawn), board.getPieceAt(1, 1));
    assertEquals("move target back to bishop\n", Optional.of(topBishop), board.getPieceAt(2, 2));
    assertEquals(false, bottomPawn.hasMoved());

    bishopAttack.apply(this.board);
    assertEquals("move source empty\n", Optional.empty(), board.getPieceAt(2, 2));
    assertEquals("move target becomes bishop\n", Optional.of(topBishop), board.getPieceAt(1, 1));
    assertEquals(true, topBishop.hasMoved());
    bishopAttack.undo(this.board);
    assertEquals("move source back to bishop\n", Optional.of(topBishop), board.getPieceAt(2, 2));
    assertEquals("move target back to pawn\n", Optional.of(bottomPawn), board.getPieceAt(1, 1));
    assertEquals(false, topBishop.hasMoved());


    knightAttack.apply(this.board);
    assertEquals("move source empty\n", Optional.empty(), board.getPieceAt(0, 3));
    assertEquals("move target becomes knight\n", Optional.of(topKnight), board.getPieceAt(1, 1));
    assertEquals(true, topKnight.hasMoved());
    knightAttack.undo(this.board);
    assertEquals("move source back to knight\n", Optional.of(topKnight), board.getPieceAt(0, 3));
    assertEquals("move target back to pawn\n", Optional.of(bottomPawn), board.getPieceAt(1, 1));
    assertEquals(false, topKnight.hasMoved());
  }
}
