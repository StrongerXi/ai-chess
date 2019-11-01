package aiChess.model;

import java.util.Optional;
import java.util.Collection;
import java.util.ArrayList;


/**
 * A model that represents the state of the chess board.
 * - queries:
 *   - dimension of the board
 *   - Piece at a position 
 *   - Reachable/Attackable Positions from a given Position. 
 * 
 * - modification:
 *   - requested move will be applied after checking boundary. 
 *   - set Piece at given position
 */
final class BoardModel {

  /**
   * Return the width of this board in unit of Piece.
   */
  public int getWidth() {
    /* TODO */
    return 0;
  }


  /**
   * Return the height of this board in unit of Piece.
   */
  public int getHeight() {
    /* TODO */
    return 0;
  }


  /**
   * Retrive the Piece at given row and column on this board.
   * @throws invalidPositionException if (row, col) is out of bound.
   * @return Optional.empty() if no piece is at position (row, ccol)
   */
  public Optional<Piece> getPieceAt(int row, int col) {
    /* TODO */
    return Optional.empty();
  }


  /**
   * Returns all the moves that the Piece at (row, col) can make.
   * @param pos the origin position represented as (row, col)
   */
  public Collection<Move> getAllMovesFrom(int row, int col) {
    /* TODO */
    return new ArrayList<>();
  }



  /**
   * Set the replacement Piece at given row and column on this board.
   * @throws invalidPositionException if (row, col) is out of bound.
   */
  public void setPieceAt(int row, int col, Optional<Piece> replacement) {
    /* TODO */
  }


  /**
   * Serialize the board state into a String, different board state
   * must return different Strings.
   */
  public String serialize() {
    /* TODO */
    return "";
  }


  @Override
  public boolean equals(Object o) {
    /* TODO */
    return false;
  }


  @Override
  public int hashCode() {
    /* TODO */
    return 0;
  }
}
