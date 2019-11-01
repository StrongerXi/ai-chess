package aiChess.model;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;


/**
 * A model that represents the state of the chess game.
 * - queries:
 *   - dimension of the game board
 *   - Piece at a position 
 *   - Reachable/Attackable Positions from a given Position. 
 *   - a history of all moves made.
 *   - serialization to String for testing purposes. 
 * 
 * - modification:
 *   - requested move will be __checked__ and then applied. (invalidMoveException)
 *   - request for undo move. (invalidUndoException)
 */
public final class ChessGameModel {

  /* The board representation  */
  private BoardModel board;

  /**
   * Return the width of the board in unit of Piece.
   */
  public int getWidth() {
    /* TODO */
    return 0;
  }


  /**
   * Return the height of the board board in unit of Piece.
   */
  public int getHeight() {
    /* TODO */
    return 0;
  }


  /**
   * Retrive the Piece at given row and column on the board.
   * @throws invalidPositionException if (row, col) is out of bound.
   * @return Optional.empty() if no piece is at position (row, ccol)
   */
  public Optional<Piece> getPieceAt(int row, int col) {
    /* TODO */
    return Optional.empty();
  }


  /**
   * Returns all the positions that the Piece at (row, col) can move to.
   * @param pos the origin position represented as (row, col)
   */
  public Collection<Position> getAllMovesFrom(int row, int col) {
    /* TODO */
    return new ArrayList<>();
  }


  /**
   * Return a list of all moves made, where Moves at lower index are made first.
   */
  public List<Move> getMoveHistory() {
    /* TODO */
    return new ArrayList<>();
  }
}
