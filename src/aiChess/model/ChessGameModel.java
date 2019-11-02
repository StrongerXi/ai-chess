package aiChess.model;

import java.util.Optional;
import java.util.Collection;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import aiChess.model.error.InvalidMoveException;
import aiChess.model.error.InvalidPositionException;
import aiChess.model.error.InvalidUndoException;


/**
 * A model that represents the state of the chess game.
 * - queries:
 *   - which player plays next
 *   - dimension of the game board
 *   - Piece at a position 
 *   - Reachable/Attackable Positions from a given Position. 
 *   - a history of all moves made.
 * 
 * - modification:
 *   - requested move will be __checked__ and then applied. (invalidMoveException)
 *   - request for undo move. (invalidUndoException)
 *
 * NOTE
 * bottom left position is encoded as (0, 0).
 */
public final class ChessGameModel {

  /* The internal board representation  */
  private BoardModel board;
  private PlayerType currentPlayer;
  private Stack<Move> moveHistory;


  /**
   * Constructor; sets up all internal states.
   * The chess board will be 8x8, and bottom player plays first.
   */
  public ChessGameModel() {
    this.board = new BoardModel(8, 8);
    this.currentPlayer = PlayerType.BOTTOM_PLAYER;
  }


  /**
   * Return the width of the board in unit of Piece.
   */
  public int getWidth() {
    return board.width;
  }


  /**
   * Return the height of the board board in unit of Piece.
   */
  public int getHeight() {
    return board.height;
  }


  /**
   * Retrive the Piece at given row and column on the board.
   * @throws InvalidPositionException if (row, col) is out of bound.
   * @return Optional.empty() if no piece is at position (row, ccol)
   */
  public Optional<Piece> getPieceAt(int row, int col) {
    if (row < 0 || row >= board.height || col < 0 || col >= board.width) {
      throw new InvalidPositionException(row, col);
    }
    return this.board.getPieceAt(row, col);
  }


  /**
   * Returns all the positions that the Piece at (row, col) can move to.
   * @param pos the origin position represented as (row, col)
   * @throws InvalidPositionException if (row, col) is out of bound.
   * NOTE if (row, col) contains no piece, empty collection will be returned.
   */
  public Collection<Position> getAllMovesFrom(int row, int col) {
    /* return only reachable positions */
    Collection<Position> targets = new ArrayList<>();
    Optional<Piece> origin = this.getPieceAt(row, col);
    if (origin.isPresent()) {
      for (Move m : origin.get().getAllMovesFrom(this.board, row, col)) {
        targets.add(m.targetPos);
      }
    }
    return targets;
  }


  /**
   * Return a list of all moves made, where Moves at lower index are made first.
   */
  public List<Move> getMoveHistory() {
    /* remove dependency */
    return new ArrayList<>(this.moveHistory);
  }


  /**
   * Return the player that is supposed to make the next move.
   */
  public PlayerType currentPlayer() {
    return this.currentPlayer;
  }


  /**
   * Undo the last move made.
   * @throws InvalidUndoException if there isn't a last move.
   */
  public void undoLastMove() {
    if (moveHistory.isEmpty()) {
      throw new InvalidUndoException("No undo available.\n");
    }
    moveHistory.pop().undo(this.board);
  }


  /**
   * Make the move which results from selecting (srow, scol), then (drow, dcol).
   * @throws InvalidMoveException if the move is not valid.
   */
  public void makeMove(int srow, int scol, int drow, int dcol) {
    /* make sure source is in bound */
    if (0 <= srow && srow < board.height && 0 <= scol && scol < board.width) {
      Optional<Piece> source = this.board.getPieceAt(srow, scol);
      Position targetPos = new Position(drow, dcol);
      /* make sure source has a piece, and target is reachable */
      if (source.isPresent()) {
        for (Move m : source.get().getAllMovesFrom(this.board, srow, scol)) {
          if (m.targetPos.equals(targetPos)) {
            /* apply the move if it's valid */
            m.apply(this.board);
            moveHistory.add(m);
          }
        }
      }
    }

    throw new InvalidMoveException(srow, scol, drow, dcol);
  }
}
