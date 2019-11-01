package aiChess.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;


/**
 * Tests for the Piece class.
 */
public class PieceTests {

  /* Test Piece equality */
  @Test
  public void testEquality() {
    /* test some samples */
    testEqualityWithTypes(PieceType.PAWN, PieceType.CASTLE);
    testEqualityWithTypes(PieceType.QUEEN, PieceType.KING);
    testEqualityWithTypes(PieceType.BISHOP, PieceType.KNIGHT);
    testEqualityWithTypes(PieceType.KING, PieceType.PAWN);
    testEqualityWithTypes(PieceType.KNIGHT, PieceType.BISHOP);
    testEqualityWithTypes(PieceType.CASTLE, PieceType.QUEEN);
  }

  /**
   * Assume t1 != t2, test how different attributes affect Piece equality.
   */
  private void testEqualityWithTypes(PieceType t1, PieceType t2) {
    assert(t1 != t2);

    Piece topT1 = PieceFactory.makePiece(t1, PlayerType.TOP_PLAYER);
    Piece bottomT2 = PieceFactory.makePiece(t1, PlayerType.BOTTOM_PLAYER);
    assertNotEquals("differ by owner", topT1, bottomT2);

    Piece topT2 = PieceFactory.makePiece(t2, PlayerType.TOP_PLAYER);
    assertNotEquals("differ by type", topT1, topT2);

    Piece topT2Dup = PieceFactory.makePiece(t2, PlayerType.TOP_PLAYER);
    assertEquals("2 unmoved\n", topT2, topT2Dup);

    topT2Dup.setMoved(true);
    assertNotEquals("1 moved 1 unmoved\n", topT2, topT2Dup);

    topT2.setMoved(true);
    assertEquals("2 moved\n", topT2, topT2Dup);
  }
}
