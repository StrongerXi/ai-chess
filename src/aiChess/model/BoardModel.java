package aiChess.model;

import java.util.Arrays;
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
   * Return a copy of this board.
   * ENSURES: No side-effect is possible between the original and copy.
   */
  BoardModel getCopy() {
    // since Piece is immutable, we just do a shallow copy
    var copy = new BoardModel(this.height, this.width);
    for (int row = 0; row < height; row += 1) {
      for (int col = 0; col < width; col += 1) {
        copy.board[row][col] = this.board[row][col];
      }
    }
    return copy;
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

  // For testing convenience
  void setPieceAt(Position pos, Optional<Piece> replacement) {
    this.setPieceAt(pos.row, pos.col, replacement);
  }

  /**
   * Return all the legal moves for `player`.
   */
  public Collection<Move> getAllLegalMoves(PlayerType player) {
    var legalMoves = new ArrayList<Move>();
    var opponent = player.getOpponent();
    // locate king and collect positions of opponent pieces
    var opponentPositions = new ArrayList<Position>();
    Position kingPosHolder = null;
    for (int row = 0; row < this.height; row += 1) {
      for (int col = 0; col < this.width; col += 1) {
        var piece = this.board[row][col];
        if (piece == null) {
          continue;

        } else if (piece.owner == opponent) {
          opponentPositions.add(Position.of(row, col));

        } else if (piece.type == PieceType.KING) {
          kingPosHolder = Position.of(row, col);
        }
      }
    }
    if (kingPosHolder == null) {
      return legalMoves; // king was captured, `player` already lost
    }
    final var kingPos = kingPosHolder; // to make lambda happy...
    // try each pseudo-legal move, and collect if legal
    for (int row = 0; row < this.height; row += 1) {
      for (int col = 0; col < this.width; col += 1) {
        var piece = this.board[row][col];
        if (piece == null || piece.owner != player) {
          continue;
        }
        piece.getAllMovesFrom(this, row, col).stream()
          .filter(m -> this.isMoveLegal(m, opponentPositions, kingPos))
          .forEach(legalMoves::add);
      }
    }
    return legalMoves;
  }

  /**
   * Check whether `move` is a legal move, that is, it
   * - Captures the opponent King
   * - OR Will not endanger its own King.
   * ASSUME:
   * - `move` is a pseudo-legal move for `this` board
   * - `opponentPositions` contains positions of all the opponent pieces
   * - `kingPos` is where the moving player's King is.
   */
  private boolean isMoveLegal(Move move, Iterable<Position> opponentPositions, Position kingPos) {
    // need to update `kingPos` in case `move` moves king
    var srcPiece = this.board[move.sourcePos.row][move.sourcePos.col];
    var dstPiece = this.board[move.targetPos.row][move.targetPos.col];
    var fKingPos = srcPiece.type == PieceType.KING ? move.targetPos : kingPos; // make lambda happy...
    // `move` captures opponent king (remember it must be pseudo-legal)
    if (dstPiece != null && dstPiece.type == PieceType.KING) {
      return true;
    }
    var opponent = srcPiece.owner.getOpponent();
    // no opponent piece should threaten king after the move
    move.apply(this);
    for (var pos : opponentPositions) {
      int row = pos.row, col = pos.col;
      var piece = this.board[row][col];
      if (piece != null &&
          piece.owner == opponent &&
          piece.getAllMovesFrom(this, row, col).stream()
          .anyMatch(m -> m.targetPos.equals(fKingPos))) {
        move.undo(this);
        return false;
      }
    }
    move.undo(this);

    return true;
  }


  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof BoardModel)) return false;
    if (o == this) return true;

    BoardModel other = (BoardModel) o;
    return Arrays.deepEquals(this.board, other.board);
  }


  @Override
  public int hashCode() {
    return Arrays.deepHashCode(this.board);
  }

  @Override
  public String toString() {
    return this.debugString();
  }

  /**
   * Convert the board to debug string format.
   */
  String debugString() {
    var sb = new StringBuilder();
    for (int row = this.height - 1; row >= 0; row -= 1) {
      for (var piece : this.board[row]) {
        var ch = (piece == null) ? 'x' : pieceToChar(piece);
        sb.append(ch);
        sb.append(' ');
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  /**
   * Convert a piece to character representation.
   */
  static private char pieceToChar(Piece p) {
    var ch = '?';
    switch (p.type) {
      case PAWN:   ch = 'p'; break;
      case KNIGHT: ch = 'n'; break;
      case BISHOP: ch = 'b'; break;
      case CASTLE: ch = 'c'; break;
      case QUEEN:  ch = 'q'; break;
      case KING:   ch = 'k'; break;
      default:     ch = '?'; break;
    }
    if (p.owner == PlayerType.TOP_PLAYER) {
      ch = Character.toUpperCase(ch);
    }
    return ch;
  }
}
