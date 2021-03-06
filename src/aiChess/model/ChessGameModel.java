package aiChess.model;

import java.util.Optional;
import java.util.Collection;
import java.util.Stack;
import java.util.stream.Collectors;
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
  private final Stack<Move> moveHistory = new Stack<>();
  private PlayerType currentPlayer;

  /* default configuration for a chess game */
  private static final int WIDTH = 8;
  private static final int HEIGHT = 8;
  private static final PieceType outterRowTypes[] =
  {PieceType.CASTLE, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN, PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.CASTLE};
  


  /**
   * Constructor; sets up all internal states.
   * The chess board will be 8x8, and bottom player plays first.
   */
  public ChessGameModel() {
    restart();
  }


  /**
   * Initialize the game state.
   */
  public void restart() {
    this.board = new BoardModel(HEIGHT, WIDTH);
    this.currentPlayer = PlayerType.BOTTOM_PLAYER;
    /* initialize the Pieces */
    for (int col = 0; col < outterRowTypes.length; col += 1) {
      Piece topPiece = PieceFactory.makePiece(outterRowTypes[col], PlayerType.TOP_PLAYER);
      Piece bottomPiece = PieceFactory.makePiece(outterRowTypes[col], PlayerType.BOTTOM_PLAYER);
      this.board.setPieceAt(board.height - 1, col, Optional.of(topPiece));
      this.board.setPieceAt(0, col, Optional.of(bottomPiece));

      Piece topPawn = PieceFactory.makePiece(PieceType.PAWN, PlayerType.TOP_PLAYER);
      Piece bottomPawn = PieceFactory.makePiece(PieceType.PAWN, PlayerType.BOTTOM_PLAYER);
      this.board.setPieceAt(board.height - 2, col, Optional.of(topPawn));
      this.board.setPieceAt(1, col, Optional.of(bottomPawn));
    }
  }


  /**
   * Construct the game with given state, for purposes such as testing.
   * @param board will be used as the current board configuration.
   * @param player will be used as the current player (to make next move)
   */
  ChessGameModel(BoardModel board, PlayerType player) {
    this.board = board;
    this.currentPlayer = player;
  }


  /**
   * Return a copy of the current board.
   * Mutating the copy has no effect on the original board.
   */
  BoardModel getBoardCopy() {
    return this.board.getCopy();
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
   * Return true if the current player has no more legal moves.
   */
  public boolean isGameOver() {
    return this.board.getCopy().getAllLegalMoves(this.currentPlayer).isEmpty();
  }


  /**
   * Returns all the positions that the Piece at (row, col) can legally move to.
   * @param pos the origin position represented as (row, col)
   * @throws InvalidPositionException if (row, col) is out of bound.
   * NOTE if (row, col) contains no piece, or it's not its owner's turn yet,
   *      empty collection will be returned.
   */
  public Collection<Position> getAllMovesFrom(int row, int col) {
    if (row < 0 || row >= board.height || col < 0 || col >= board.width) {
      throw new InvalidPositionException(row, col);
    }
    var sourcePos = Position.of(row, col);
    return
      this.board.getCopy().getAllLegalMoves(this.currentPlayer).stream()
      .filter(m -> m.sourcePos.equals(sourcePos))
      .map(m -> m.targetPos).collect(Collectors.toList());
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
  public PlayerType getCurrentPlayer() {
    return this.currentPlayer;
  }


  /**
   * Undo the last move made.
   * @throws InvalidUndoException if there isn't a last move.
   */
  public void undoLastMove() {
    if (moveHistory.isEmpty()) {
      throw new InvalidUndoException("No undo available.");
    }
    this.moveHistory.pop().undo(this.board);
    this.switchPlayer();
  }


  /**
   * Switch the current player.
   */
  private void switchPlayer() {
    this.currentPlayer = this.currentPlayer.getOpponent();
  }


  /**
   * Make the move which results from selecting (srow, scol), then (drow, dcol).
   * @throws InvalidMoveException if 
   *         - Origin is empty
   *         - Not this player's turn yet
   *         - Move is invalid.
   */
  public void makeMove(int srow, int scol, int drow, int dcol) {
    System.out.printf("requested move (%d, %d) to (%d, %d)\n", srow, scol, drow, dcol);
    /* make sure source is in bound */
    if (srow < 0 || srow >= board.height || scol < 0 || scol >= board.width) {
      throw new InvalidMoveException("source or target out of bound");
    }

    var source = this.board.getPieceAt(srow, scol);
    var sourcePos = Position.of(srow, scol);
    var targetPos = Position.of(drow, dcol);
    /* make sure source has a piece, and target is reachable */
    if (!source.isPresent()) {
      throw new InvalidMoveException("origin can't be empty");
    }

    if (source.get().owner != currentPlayer) {
      throw new InvalidMoveException("Not this player's turn yet");
    }

    /* check requested move against each possible moves */
    for (Move m : this.board.getAllLegalMoves(this.currentPlayer)) {
      if (m.sourcePos.equals(sourcePos) && m.targetPos.equals(targetPos)) {
        /* apply the move if it's valid */
        m.apply(this.board);
        moveHistory.add(m);
        this.switchPlayer();
        return;
      }
    }

    /* the move is not valid */
    throw new InvalidMoveException(srow, scol, drow, dcol);
  }
}
