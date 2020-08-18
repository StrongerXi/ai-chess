package aiChess.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;


/**
 * Tests for the Move class.
 */
public class MoveTest {

  /* Test how different attributes affect Regular Move equality */
  @Test
  public void testRegularMoveEquality() {
    Position pos1 = new Position(2, 2);
    Position pos2 = new Position(1, 3);
    Position pos3 = new Position(4, 2);
    Position pos4 = new Position(3, 0);
    Move m1 = MoveFactory.makeRegularMove(pos1, pos2);
    Move m2 = MoveFactory.makeRegularMove(pos1, pos3);
    Move m3 = MoveFactory.makeRegularMove(pos3, pos2);
    Move m4 = MoveFactory.makeRegularMove(pos4, pos4);

    assertEquals("same positions\n", m1, MoveFactory.makeRegularMove(pos1, pos2));
    assertEquals("same positions\n", m2, MoveFactory.makeRegularMove(pos1, pos3));
    assertEquals("same positions\n", m3, MoveFactory.makeRegularMove(pos3, pos2));
    assertEquals("same positions\n", m4, MoveFactory.makeRegularMove(pos4, pos4));

    /* differ in different attributes */
    assertNotEquals("differ in source position\n", m1, m3);
    assertNotEquals("differ in target position\n", m1, m2);
    assertNotEquals("differ in both position\n", m1, m4);
  }

  /* Test how different attributes affect Pawn Promotion Move equality */
  @Test
  public void testPawnPromotionMove() {
    Position pos1 = new Position(2, 2);
    Position pos2 = new Position(1, 3);
    Position pos3 = new Position(4, 2);
    Position pos4 = new Position(3, 0);
    Move m1 = MoveFactory.makePawnPromotion(pos1, pos2);
    Move m2 = MoveFactory.makePawnPromotion(pos1, pos3);
    Move m3 = MoveFactory.makePawnPromotion(pos3, pos2);
    Move m4 = MoveFactory.makePawnPromotion(pos4, pos4);

    assertEquals("same positions\n", m1, MoveFactory.makePawnPromotion(pos1, pos2));
    assertEquals("same positions\n", m2, MoveFactory.makePawnPromotion(pos1, pos3));
    assertEquals("same positions\n", m3, MoveFactory.makePawnPromotion(pos3, pos2));
    assertEquals("same positions\n", m4, MoveFactory.makePawnPromotion(pos4, pos4));

    /* differ in different attributes */
    assertNotEquals("differ in source position\n", m1, m3);
    assertNotEquals("differ in target position\n", m1, m2);
    assertNotEquals("differ in both position\n", m1, m4);
  }
}
