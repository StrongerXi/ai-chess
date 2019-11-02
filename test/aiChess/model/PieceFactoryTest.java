package aiChess.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;
import org.junit.Before;

import java.util.Optional;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Tests for the PieceFactory class.
 * Mainly the logic of move generation. 
 */
public class PieceFactoryTest {

  /* a 8x8 board to fascilitate tests */
  private BoardModel board;
  @Before
  public void initBoard() {
    this.board = new BoardModel(8, 8);
  }


  /* Test move logic for different pieces 
   * NOTE:
   * top player's pieces will be written as uppercase in comments. */ 


  /* test forward moves of pawns */
  @Test
  public void testPawnsForwardMove() {
    /* _ P
     * _ _ _ P _ _
     * _ _ _ _ _ P
     * _ p _ p _ p
     * _
     * _
     */
    Piece topPawn = PieceFactory.makePiece(PieceType.PAWN, PlayerType.TOP_PLAYER);
    Piece bottomPawn = PieceFactory.makePiece(PieceType.PAWN, PlayerType.BOTTOM_PLAYER);
    this.board.setPieceAt(2, 1, Optional.of(bottomPawn));
    this.board.setPieceAt(2, 3, Optional.of(bottomPawn));
    this.board.setPieceAt(2, 5, Optional.of(bottomPawn));
    this.board.setPieceAt(5, 1, Optional.of(topPawn));
    this.board.setPieceAt(4, 3, Optional.of(topPawn));
    this.board.setPieceAt(3, 5, Optional.of(topPawn));

    /* unmoved pawn has 2 possible moves */
    Set<Move> pawn51Moves = new HashSet<>(topPawn.getAllMovesFrom(this.board, 5, 1));
    Set<Move> pawn21Moves = new HashSet<>(bottomPawn.getAllMovesFrom(this.board, 2, 1));
    Set<Move> pawn51ExpectedMoves = new HashSet<>();
    Set<Move> pawn21ExpectedMoves = new HashSet<>();
    pawn51ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(5, 1), new Position(4, 1)));
    pawn51ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(5, 1), new Position(3, 1)));
    pawn21ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(2, 1), new Position(3, 1)));
    pawn21ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(2, 1), new Position(4, 1)));
    assertEquals("downward 2 possible moves\n", pawn51ExpectedMoves, pawn51Moves);
    assertEquals("upward 2 possible moves\n", pawn21ExpectedMoves, pawn21Moves);

    /* unmoved pawn where 2-stride move is blocked */
    Set<Move> pawn43Moves = new HashSet<>(topPawn.getAllMovesFrom(this.board, 4, 3));
    Set<Move> pawn23Moves = new HashSet<>(bottomPawn.getAllMovesFrom(this.board, 2, 3));
    Set<Move> pawn43ExpectedMoves = new HashSet<>();
    Set<Move> pawn23ExpectedMoves = new HashSet<>();
    pawn43ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(4, 3), new Position(3, 3)));
    pawn23ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(2, 3), new Position(3, 3)));
    assertEquals("downward 1 possible moves\n", pawn43ExpectedMoves, pawn43Moves);
    assertEquals("upward 1 possible moves\n", pawn23ExpectedMoves, pawn23Moves);

    /* unmoved pawn where both forward moves are blocked */
    Set<Move> pawn35Moves = new HashSet<>(topPawn.getAllMovesFrom(this.board, 3, 5));
    Set<Move> pawn25Moves = new HashSet<>(bottomPawn.getAllMovesFrom(this.board, 2, 5));
    Set<Move> pawn35ExpectedMoves = new HashSet<>();
    Set<Move> pawn25ExpectedMoves = new HashSet<>();
    assertEquals("forward move blocked\n", pawn35ExpectedMoves, pawn35Moves);
    assertEquals("forward move blocked\n", pawn25ExpectedMoves, pawn25Moves);


    /* set all pawns to moved state */
    topPawn.setMoved(true);
    bottomPawn.setMoved(true);

    /* moved pawn has 1 possible moves */
    pawn51Moves = new HashSet<>(topPawn.getAllMovesFrom(this.board, 5, 1));
    pawn21Moves = new HashSet<>(bottomPawn.getAllMovesFrom(this.board, 2, 1));
    pawn51ExpectedMoves = new HashSet<>();
    pawn21ExpectedMoves = new HashSet<>();
    pawn51ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(5, 1), new Position(4, 1)));
    pawn21ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(2, 1), new Position(3, 1)));
    assertEquals("downward 1 possible moves\n", pawn51ExpectedMoves, pawn51Moves);
    assertEquals("upward 1 possible moves\n", pawn21ExpectedMoves, pawn21Moves);
  }


  /* test diagonal moves of pawns */
  @Test
  public void testPawnsDiagonalMove() {
    /* _ _
     * _ _ _ _ _ _
     * _ _ P _ _ _
     * _ p _ p _ _
     * _ _ p
     * _
     */
    Piece topPawn = PieceFactory.makePiece(PieceType.PAWN, PlayerType.TOP_PLAYER);
    Piece bottomPawn = PieceFactory.makePiece(PieceType.PAWN, PlayerType.BOTTOM_PLAYER);
    this.board.setPieceAt(2, 1, Optional.of(bottomPawn));
    this.board.setPieceAt(2, 3, Optional.of(bottomPawn));
    this.board.setPieceAt(1, 2, Optional.of(bottomPawn));
    this.board.setPieceAt(3, 2, Optional.of(topPawn));

    /* top pawn can attack 2 bottom pawns */
    Set<Move> pawn32Moves = new HashSet<>(topPawn.getAllMovesFrom(this.board, 3, 2));
    Set<Move> pawn32ExpectedMoves = new HashSet<>();
    pawn32ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(3, 2), new Position(2, 3)));
    pawn32ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(3, 2), new Position(2, 1)));
    pawn32ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(3, 2), new Position(2, 2)));
    assertEquals("diagonal 2, forward 1 moves\n", pawn32ExpectedMoves, pawn32Moves);

    /* right pawn can attack top pawn */
    Set<Move> pawn21Moves = new HashSet<>(bottomPawn.getAllMovesFrom(this.board, 2, 1));
    Set<Move> pawn21ExpectedMoves = new HashSet<>();
    pawn21ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(2, 1), new Position(3, 2)));
    pawn21ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(2, 1), new Position(3, 1)));
    pawn21ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(2, 1), new Position(4, 1)));
    assertEquals("right up diagonal 1, forward 2 move\n", pawn21ExpectedMoves, pawn21Moves);

    /* right pawn can attack top pawn */
    Set<Move> pawn23Moves = new HashSet<>(bottomPawn.getAllMovesFrom(this.board, 2, 3));
    Set<Move> pawn23ExpectedMoves = new HashSet<>();
    pawn23ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(2, 3), new Position(3, 2)));
    pawn23ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(2, 3), new Position(3, 3)));
    pawn23ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(2, 3), new Position(4, 3)));
    assertEquals("left up diagonal 1, forward 2 move\n", pawn23ExpectedMoves, pawn23Moves);

    /* bottom pawn can't attack ally */
    Set<Move> pawn12Moves = new HashSet<>(bottomPawn.getAllMovesFrom(this.board, 1, 2));
    Set<Move> pawn12ExpectedMoves = new HashSet<>();
    pawn12ExpectedMoves.add(MoveFactory.makeRegularMove(new Position(1, 2), new Position(2, 2)));
    assertEquals("can't attack ally, only forward 1 move\n", pawn12ExpectedMoves, pawn12Moves);
  }


  /* test moves of castle in all directions, against enemy and ally */
  @Test
  public void testCastleMove() {
    /* _ _ p
     * _ _ _
     * _ _ _ _ _
     * P _ C _ _ _ _ P
     * _ _ p
     */
    Piece topCastle = PieceFactory.makePiece(PieceType.CASTLE, PlayerType.TOP_PLAYER);
    Piece topPawn = PieceFactory.makePiece(PieceType.PAWN, PlayerType.TOP_PLAYER);
    Piece bottomPawn = PieceFactory.makePiece(PieceType.PAWN, PlayerType.BOTTOM_PLAYER);
    this.board.setPieceAt(1, 2, Optional.of(topCastle));
    this.board.setPieceAt(0, 2, Optional.of(bottomPawn)); // bottom
    this.board.setPieceAt(1, 0, Optional.of(topPawn)); // left
    this.board.setPieceAt(4, 2, Optional.of(bottomPawn)); // top
    this.board.setPieceAt(1, 7, Optional.of(topPawn)); // right

    Set<Move> castleMoves = new HashSet<>(topCastle.getAllMovesFrom(this.board, 1, 2));
    Set<Move> castleExpectedMoves = new HashSet<>();
    Position castlePos = new Position(1, 2);
    /* castle can attack bottom enemy pawn */
    castleExpectedMoves.add(MoveFactory.makeRegularMove(castlePos, new Position(0, 2)));
    /* castle can't attack left ally pawn */
    castleExpectedMoves.add(MoveFactory.makeRegularMove(castlePos, new Position(1, 1)));
    /* castle can attack top enemy pawn */
    castleExpectedMoves.add(MoveFactory.makeRegularMove(castlePos, new Position(2, 2)));
    castleExpectedMoves.add(MoveFactory.makeRegularMove(castlePos, new Position(3, 2)));
    castleExpectedMoves.add(MoveFactory.makeRegularMove(castlePos, new Position(4, 2)));
    /* castle can't attack right ally pawn */
    castleExpectedMoves.add(MoveFactory.makeRegularMove(castlePos, new Position(1, 3)));
    castleExpectedMoves.add(MoveFactory.makeRegularMove(castlePos, new Position(1, 4)));
    castleExpectedMoves.add(MoveFactory.makeRegularMove(castlePos, new Position(1, 5)));
    castleExpectedMoves.add(MoveFactory.makeRegularMove(castlePos, new Position(1, 6)));

    assertEquals("test castle moves\n", castleExpectedMoves, castleMoves);
  }


  /* test moves of knight in all directions, against enemy and ally */
  @Test
  public void testKnightMove() {
    /*
     * _ P _ b _
     * p _ _ _ K
     * _ _ K _ _
     * p _ _ _ k
     * _ P _ C _
     */

    Piece topKnight = PieceFactory.makePiece(PieceType.KNIGHT, PlayerType.TOP_PLAYER);
    Piece topCastle = PieceFactory.makePiece(PieceType.CASTLE, PlayerType.TOP_PLAYER);
    Piece topPawn = PieceFactory.makePiece(PieceType.PAWN, PlayerType.TOP_PLAYER);
    Piece bottomBishop = PieceFactory.makePiece(PieceType.BISHOP, PlayerType.BOTTOM_PLAYER);
    Piece bottomKnight = PieceFactory.makePiece(PieceType.KNIGHT, PlayerType.BOTTOM_PLAYER);
    Piece bottomPawn = PieceFactory.makePiece(PieceType.PAWN, PlayerType.BOTTOM_PLAYER);

    this.board.setPieceAt(2, 2, Optional.of(topKnight)); // center
    this.board.setPieceAt(4, 3, Optional.of(bottomBishop)); // up right
    this.board.setPieceAt(3, 4, Optional.of(topKnight)); // right up
    this.board.setPieceAt(1, 4, Optional.of(bottomKnight)); // right down
    this.board.setPieceAt(0, 3, Optional.of(topCastle)); // down right
    this.board.setPieceAt(0, 1, Optional.of(topPawn)); // down left
    this.board.setPieceAt(1, 0, Optional.of(bottomPawn)); // left down
    this.board.setPieceAt(3, 0, Optional.of(bottomPawn)); // left up
    this.board.setPieceAt(4, 1, Optional.of(topPawn)); // up left


    /* only enemy positions are attackable */
    Set<Move> knightMoves = new HashSet<>(topKnight.getAllMovesFrom(this.board, 2, 2));
    Set<Move> knightExpectedMoves = new HashSet<>();
    Position knightPos = new Position(2, 2);

    /* up right bishop */
    knightExpectedMoves.add(MoveFactory.makeRegularMove(knightPos, new Position(4, 3)));
    /* right down knight */
    knightExpectedMoves.add(MoveFactory.makeRegularMove(knightPos, new Position(1, 4)));
    /* left up pawn */
    knightExpectedMoves.add(MoveFactory.makeRegularMove(knightPos, new Position(3, 0)));
    /* left down pawn */
    knightExpectedMoves.add(MoveFactory.makeRegularMove(knightPos, new Position(1, 0)));

    assertEquals("test knight moves\n", knightExpectedMoves, knightMoves);
  }


  /* test moves of bishop in all directions, against enemy and ally */
  @Test
  public void testBishopMove() {
  }


  /* test moves of queen in all directions, against enemy and ally */
  @Test
  public void testQueenMove() {
  }


  /* test moves of king in all directions, against enemy and ally */
  @Test
  public void testKingMove() {
  }
}
