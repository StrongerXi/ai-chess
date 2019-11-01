package aiChess.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;
import org.junit.Before;

import java.util.Optional;
import java.util.Collection;


/**
 * Tests for the PieceFactory class.
 * Mainly the logic of move generation. 
 */
public class PieceFactoryTests {

  /* a 8x8 board to fascilitate tests */
  private BoardModel board;
  @Before
  private void initBoard() {
    this.board = new BoardModel(8, 8);
  }


  /* Test move logic for different pieces 
   * NOTE:
   * top player's pieces will be written as uppercase in comments. */ 

  /* test pawn forward moves */
  @Test
  public void testPawnMove() {
    /* _ P
     * _ _ _ P _ _
     * _ _ _ _ _ P
     * _ p _ p _ p
     * _
     * _
     */
    Piece pawn51 = PieceFactory.makePiece(PieceType.PAWN, PlayerType.TOP_PLAYER);
    Piece pawn21 = PieceFactory.makePiece(PieceType.PAWN, PlayerType.BOTTOM_PLAYER);
    Piece pawn43 = PieceFactory.makePiece(PieceType.PAWN, PlayerType.TOP_PLAYER);
    Piece pawn23 = PieceFactory.makePiece(PieceType.PAWN, PlayerType.BOTTOM_PLAYER);
    Piece pawn35 = PieceFactory.makePiece(PieceType.PAWN, PlayerType.TOP_PLAYER);
    Piece pawn25 = PieceFactory.makePiece(PieceType.PAWN, PlayerType.BOTTOM_PLAYER);
    this.board.setPieceAt(2, 1, Optional.of(pawn21));
    this.board.setPieceAt(2, 3, Optional.of(pawn23));
    this.board.setPieceAt(2, 5, Optional.of(pawn25));
    this.board.setPieceAt(5, 1, Optional.of(pawn51));
    this.board.setPieceAt(4, 3, Optional.of(pawn43));
    this.board.setPieceAt(3, 5, Optional.of(pawn35));
    Collection<Move> pawn51Moves = pawn51.getAllMovesFrom(this.board, 5, 1);
    Collection<Move> pawn21Moves = pawn21.getAllMovesFrom(this.board, 2, 1);

    /* unmoved pawn has 2 possible moves */
    assertEquals("2 possible moves\n", pawn51Moves.size(), 2);
    assertEquals("2 possible moves\n", pawn21Moves.size(), 2);
  }
}
