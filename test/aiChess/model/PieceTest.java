package aiChess.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;


/**
 * Tests for the Piece class.
 */
public class PieceTest {

  /* Test Piece equality */
  @Test
  public void testEquality() {
    // test some samples
    for (var t1 : PieceType.values()) {
      testImmutability(t1);
      for (var t2 : PieceType.values()) {
        if (t1 != t2) {
          testEqualityWithTypes(t1, t2);
        }
      }
    }
  }

  /**
   * Assume t1 != t2, test how different attributes affect Piece equality.
   */
  private void testEqualityWithTypes(PieceType t1, PieceType t2) {
    assert(t1 != t2);
    var info = String.format("[%s vs %s] ", t1.toString(), t2.toString());

    Piece topT1 = PieceFactory.makePiece(t1, PlayerType.TOP_PLAYER);
    Piece bottomT2 = PieceFactory.makePiece(t1, PlayerType.BOTTOM_PLAYER);
    assertNotEquals(info + "differ by owner", topT1, bottomT2);

    Piece topT2 = PieceFactory.makePiece(t2, PlayerType.TOP_PLAYER);
    assertNotEquals(info + "differ by type", topT1, topT2);

    Piece topT2Dup = PieceFactory.makePiece(t2, PlayerType.TOP_PLAYER);
    assertEquals(info + "2 unmoved\n", topT2, topT2Dup);

    topT2Dup = topT2Dup.setMoved(true);
    assertNotEquals(info + "1 moved 1 unmoved\n", topT2, topT2Dup);

    topT2 = topT2.setMoved(true);
    assertEquals(info + "2 moved\n", topT2, topT2Dup);
  }


  /**
   * Make sure setMoved works and won't mutate the original piece.
   */
  private void testImmutability(PieceType t1) {
    var info = String.format("[%s] ", t1.toString());
    var topT1 = PieceFactory.makePiece(t1, PlayerType.TOP_PLAYER);
    var bottomT1 = PieceFactory.makePiece(t1, PlayerType.BOTTOM_PLAYER);
    assertEquals(info + "top original", false, topT1.hasMoved);
    assertEquals(info + "bottom original", false, bottomT1.hasMoved);

    var topT1Moved = topT1.setMoved(true);
    var bottomT1Moved = topT1.setMoved(true);
    assertEquals(info + "top original", false, topT1.hasMoved);
    assertEquals(info + "bottom original", false, bottomT1.hasMoved);
    assertEquals(info + "top moved", true, topT1Moved.hasMoved);
    assertEquals(info + "bottom moved", true, bottomT1Moved.hasMoved);

    var topT1Unmoved = topT1Moved.setMoved(false);
    var bottomT1Unmoved = topT1Moved.setMoved(false);
    assertEquals(info + "top original", false, topT1.hasMoved);
    assertEquals(info + "bottom original", false, bottomT1.hasMoved);
    assertEquals(info + "top moved", true, topT1Moved.hasMoved);
    assertEquals(info + "bottom moved", true, bottomT1Moved.hasMoved);
    assertEquals(info + "top unmoved", false, topT1Unmoved.hasMoved);
    assertEquals(info + "bottom unmoved", false, bottomT1Unmoved.hasMoved);
  }
}
