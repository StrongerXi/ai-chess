package aiChess.model;

/**
 * A Position that contains a row and column index.
 */
public final class Position {

  /* Read Only attributes */
  public final int row;
  public final int col;

  /**
   * Constructor.
   */
  public Position(int row, int col) {
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
