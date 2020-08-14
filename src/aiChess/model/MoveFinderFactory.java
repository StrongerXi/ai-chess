package aiChess.model;


/**
 * A factory to generate different types of MoveFinder.
 */
public final class MoveFinderFactory {

  public enum MoveFinderType {
    MINIMAX, ALPHA_BETA
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
    switch (type) {
      case MINIMAX:    return new Minimax(depth);
      case ALPHA_BETA: return new AlphaBeta(depth);
      default:
        throw new RuntimeException("Unsupported MoveFinderType: " + type.toString());
    }
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
        System.out.printf("score = %d, bestScore = %d, move = %s\n", score, bestScore, move);
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
  }

  /**
   * MoveFinder based on minimax search algorithm with alpha-beta pruning.
   */
  private static final class AlphaBeta implements MoveFinder {
    private final int depth;
    /**
     * Constructor.
     */
    AlphaBeta(int depth) {
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

      // do 1 step expansion here, since `alphabeta` returns score only.
      for (var move : legalMoves) {
        move.apply(board);
        var score = this.alphabeta(board, this.depth - 1, bestScore, Integer.MAX_VALUE, opponent, player);
        // System.out.printf("score = %d, bestScore = %d, move = %s\n", score, bestScore, move);
        if (bestMove == null || score > bestScore) { // must be `>`
          bestMove = move;
          bestScore = score;
        }
        move.undo(board);
      }
      return bestMove;
    }

    /**
     * Evaluate given board based on the minimax algorithm with alpha-beta pruning.
     * **NOTE**:
     * If
     * 1. this is used to find the move with best score by a top-level caller,
     * 2. the caller updates alpha with current best score
     * Then it must replace the best move only when a new score **exceeds** the old best score.
     * Because if they are equal, the new score might just got trimmed, i.e. it didn't consider 
     * other cases in the branch which could be worse.
     * @param board          is the current board state.
     * @param remainDepth    is how many more recursions to take.
     * @param alpha          is the minimum score originalPlayer is guaranteed to achieve.
     * @param beta           is the maximum score originalPlayer is guaranteed to achieve.
     * @param currentPlayer  is the player to make next move.
     * @param originalPlayer is the player for which we evaluate board for.
     */
    private int alphabeta(BoardModel board, int remainDepth, int alpha, int beta, PlayerType currentPlayer, PlayerType originalPlayer) {
      if (remainDepth == 0) {
        return evaluateBoard(board, originalPlayer);
      }
      boolean maximizer = (currentPlayer == originalPlayer);
      var nextPlayer = flipPlayer(currentPlayer);
      var score = maximizer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
      for (var move : board.getAllLegalMoves(currentPlayer)) {
        move.apply(board);
        var newScore = this.alphabeta(board, remainDepth - 1, alpha, beta, nextPlayer, originalPlayer);
        move.undo(board);
        score = maximizer ?
                Integer.max(score, newScore) :
                Integer.min(score, newScore);
        if (maximizer) {
          alpha = Integer.max(alpha, score);
        } else {
          beta = Integer.min(beta, score);
        }
        // no need to continue further, since this branch won't make things better for either player
        // e.g. originalPlayer can already ensure a score = 10, if opponent won't let it get
        // anything better than 10, then what's the point of continuing in this branch?
        if (alpha >= beta) {
          break;
        }
      }
      return score;
    }
  }

  /**
   * Evaluate the state of `board` for `player`.
   * Higher score means better chance of winning.
   */
  static private int evaluateBoard(BoardModel board, PlayerType player) {
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
