package aiChess.model;

import java.util.Map;
import java.util.HashMap;

/**
 * A Position that contains a row and column index.
 */
public final class Position {

  public static Position of(int row, int col) {
    return (row < 0 || row >= CACHE_SIZE || col < 0 || col >= CACHE_SIZE) ?
            new Position(row, col) :
            cache[row * CACHE_SIZE + col];
  }

  // Most chess board won't go above (or even get close to) this limit.
  // This avoids excessive allocation of Piece instances within a chess board.
  private static final int CACHE_SIZE = 100;
  private static final Position[] cache = new Position[CACHE_SIZE * CACHE_SIZE];
  static {
    for (int row = 0; row < CACHE_SIZE; row += 1) {
      for (int col = 0; col < CACHE_SIZE; col += 1) {
        cache[row * CACHE_SIZE + col] = new Position(row, col);
      }
    }
  }

  /* Read Only attributes */
  public final int row;
  public final int col;

  /**
   * Position instances can only be obtained via static methods.
   */
  private Position(int row, int col) {
    this.row = row;
    this.col = col;
  }


  @Override
  public boolean equals(Object other) {
    if (other == null) return false;
    if (! (other instanceof Position)) return false;
    Position otherPos = (Position) other;
    return row == otherPos.row && col == otherPos.col;
  }


  @Override
  public int hashCode() {
    return 31 * Integer.hashCode(row) + 103 * Integer.hashCode(col);
  }


  public String toString() {
    return String.format("(%d, %d)", row, col);
  }
}
