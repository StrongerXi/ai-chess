package aiChess.model;


/**
 * A factory to generate different types of MoveFinder.
 */
public final class MoveFinderFactory {

  public enum MoveFinderType {
    MINIMAX
  }

  /* Prevent instantiation */
  private MoveFinderFactory() {}

  /**
   * Return the other player.
   */
  static PlayerType flipPlayer(PlayerType player) {
    return (player == PlayerType.TOP_PLAYER) ?
            PlayerType.BOTTOM_PLAYER : PlayerType.TOP_PLAYER;
  }

  /**
   * Generate a MoveFinder instance based on `type`, with `depth` being
   * maximum search depth.
   */
  public static MoveFinder makeMoveFinder(MoveFinderType type, int depth) {
    return new Minimax(depth);
  }

  /**
   * MoveFinder based on the naive minimax search algorithm.
   */
  private static final class Minimax implements MoveFinder {
    private final int depth;
    /**
     * Constructor.
     */
    Minimax(int depth) {
      this.depth = depth;
    }

    public Move getBestMove(ChessGameModel model) {
      var player = model.getCurrentPlayer();
      var board = model.getBoardCopy();
      var legalMoves = board.getAllLegalMoves(player);
      assert(!legalMoves.isEmpty());

      // find the move with best score
      Move bestMove = null;
      var bestScore = Integer.MIN_VALUE;
      var opponent = flipPlayer(player);

      // do 1 step expansion here, since `minimax` returns score only.
      for (var move : legalMoves) {
        move.apply(board);
        var score = this.minimax(board, this.depth - 1, opponent, player);
        if (score >= bestScore) { // == in case all moves end in checkmate
          bestMove = move;
          bestScore = score;
        }
        move.undo(board);
      }
      return bestMove;
    }

    /**
     * Evaluate given board based on the minimax algorithm.
     * @param board          is the current board state.
     * @param remainDepth    is how many more recursions to take.
     * @param currentPlayer  is the player to make next move.
     * @param originalPlayer is the player for which we evaluate board for.
     */
    private int minimax(BoardModel board, int remainDepth, PlayerType currentPlayer, PlayerType originalPlayer) {
      if (remainDepth == 0) {
        return evaluateBoard(board, originalPlayer);
      }
      boolean maximizer = (currentPlayer == originalPlayer);
      var nextPlayer = flipPlayer(currentPlayer);
      var score = maximizer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
      for (var move : board.getAllLegalMoves(currentPlayer)) {
        move.apply(board);
        var newScore = this.minimax(board, remainDepth - 1, nextPlayer, originalPlayer);
        score = maximizer ?
                Integer.max(score, newScore) :
                Integer.min(score, newScore);
        move.undo(board);
      }
      return score;
    }

    /**
     * Evaluate the state of `board` for `player`.
     * Higher score means better chance of winning.
     */
    private int evaluateBoard(BoardModel board, PlayerType player) {
      int score = 0;
      for (int row = 0; row < board.height; row += 1) {
        for (int col = 0; col < board.width; col += 1) {
          var piece = board.getPieceAt(row, col);
          if (piece.isEmpty()) {
            continue;
          }
          var pieceScore = 0;
          switch(piece.get().type) {
            case PAWN:   pieceScore = 10;  break;
            case KNIGHT: pieceScore = 30;  break;
            case BISHOP: pieceScore = 30;  break;
            case CASTLE: pieceScore = 50;  break;
            case QUEEN:  pieceScore = 90;  break;
            case KING:   pieceScore = 900; break;
          }
          if (player != piece.get().owner) {
            pieceScore *= -1;
          }
          score += pieceScore;
        }
      }
      return score;
    }
  }
}
