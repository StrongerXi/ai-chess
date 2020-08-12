package aiChess.model;

import java.util.Collection;

/**
 * A value class that represents a general Piece.
 * Supports query for:
 * - who owns this piece
 * - what type of piece it is
 * - whether it has moved
 * - move logic (board, position ==> pseudo-viable moves)
 */
public abstract class Piece {

  /* Read-Only attributes */
  public final PlayerType owner;
  public final PieceType type;
  public final boolean hasMoved;

  /**
   * Abstract constructor, can't be instantiated.
   */
  Piece(PlayerType owner, PieceType type) {
    this(owner, type, false);
  }

  /**
   * Abstract constructor, can't be instantiated.
   */
  Piece(PlayerType owner, PieceType type, boolean hasMoved) {
    this.owner = owner;
    this.type = type;
    this.hasMoved = hasMoved;
  }

  /**
   * Return a copy of this Piece, with hasMoved updated to given state.
   */
  abstract Piece setMoved(boolean state);


  /**
   * Return all moves that can be made if this Piece is currently at
   * (row, col) on the given board. 
   * (Do not consider whether this will result in a check)
   */
  abstract Collection<Move> getAllMovesFrom(BoardModel board, int row, int col);


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


  @Override
  public String toString() {
    return String.format("(%s, %s, moved: %b)", 
        owner.toString(), type.toString(), this.hasMoved);
  }
}
