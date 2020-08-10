package aiChess.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import aiChess.model.error.InvalidPositionException;

/**
 * A model that represents the state of the chess board.
 * - queries:
 *   - dimension of the board
 *   - Piece at a position 
 * 
 * - modification:
 *   - requested move will be applied after checking boundary. 
 *   - set Piece at given position
 *
 * NOTE
 * bottom left position is encoded as (0, 0).
 */
final class BoardModel {

  /* dimension of the board, >= 0 */
  public final int width;
  public final int height;

  /* empty pieces are represented by null */
  private final Piece[][] board;

  /**
   * Initialize a board with given width and height.
   * @param height max number of pieces placed in a column
   * @param width max number of pieces placed in a row
   * @throws IllegalArgumentException
   */
  BoardModel(int height, int width) {
    if (width < 0 || height < 0) {
      String msg = String.format(
            "Board dimensions can't be negative: width = %d, height = %d\n", 
            width, height);
      throw new IllegalArgumentException(msg);
    }

    this.width = width;
    this.height = height;
    this.board = new Piece[height][width];
  }

  /**
   * Retrive the Piece at given row and column on this board.
   * @throws InvalidPositionException if (row, col) is out of bound.
   * @return Optional.empty() if no piece is at position (row, ccol)
   */
  public Optional<Piece> getPieceAt(int row, int col) {
    if (row < 0 || row >= this.height || col < 0 || col >= this.width) {
      throw new InvalidPositionException(row, col);
    }
    return Optional.ofNullable(this.board[row][col]);
  }


  /**
   * Set the replacement Piece at given row and column on this board.
   * @throws InvalidPositionException if (row, col) is out of bound.
   */
  public void setPieceAt(int row, int col, Optional<Piece> replacement) {
    if (row < 0 || row >= this.height || col < 0 || col >= this.width) {
      throw new InvalidPositionException(row, col);
    }
    this.board[row][col] = replacement.isPresent() ? replacement.get() : null;
  }


  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof BoardModel)) return false;
    if (o == this) return true;

    BoardModel other = (BoardModel) o;
    /* compare each piece */
    for (int r = 0; r < height; r += 1) {
      for (int c = 0; c < width; c += 1) {
        if (!this.board[r][c].equals(other.board[r][c])) return false;
      }
    }
    return true;
  }


  @Override
  public int hashCode() {
    return board.hashCode();
  }
}
