package aiChess.model;

import java.util.Collection;

/**
 * Represents a general Piece.
 * Supports query for:
 * - who owns this piece
 * - what type of piece it is
 * - whether it has moved
 * - move logic (board, position ==> viable moves)
 *
 * NOTE: 
 * Except for the hasMoved? property, it acts like a value class and 
 * the other attributes are completely immutable.
 */
public abstract class Piece {

  /* Read-Only attributes */
  public final PlayerType owner;
  public final PieceType type;

  /* set to true after first move */
  private boolean hasMoved;


  /**
   * Abstract constructor, can't be instantiated.
   */
  Piece(PlayerType owner, PieceType type) {
    this.owner = owner;
    this.type = type;
  }


  /**
   * Returns true iff this piece has been moved.
   */
  public boolean hasMoved() {
    return this.hasMoved;
  }


  /**
   * Set this Piece as already moved iff given state is true.
   */
  void setMoved(boolean state) {
    this.hasMoved = state;
  }


  @Override
  public int hashCode() {
    return 
      ((1 + this.owner.hashCode() * 31) * 31 
       + this.type.hashCode()) * 31 + Boolean.hashCode(this.hasMoved);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null || !(o instanceof Piece)) return false;

    Piece other = (Piece) o;
    return this.type == other.type 
        && this.owner == other.owner
        && this.hasMoved == other.hasMoved;
  }


  /**
   * Return all moves that can be made if this Piece is currently at
   * (row, col) on the given board.
   */
  abstract Collection<Move> getAllMovesFrom(BoardModel board, int row, int col);
}
