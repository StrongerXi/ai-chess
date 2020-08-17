package aiChess.model;

import aiChess.model.TranspositionTable.EntryType;
import java.util.Collection;

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
   * Generate a MoveFinder instance for `player`, based on `type`, with `depth` being
   * maximum search depth.
   */
  public static MoveFinder makeMoveFinder(MoveFinderType type, int depth, PlayerType player) {
    switch (type) {
      case MINIMAX:    return new Minimax(depth, player);
      case ALPHA_BETA: return new AlphaBeta(depth, player);
      default:
        throw new RuntimeException("Unsupported MoveFinderType: " + type.toString());
    }
  }

  /**
   * MoveFinder based on the naive minimax search algorithm.
   */
  private static final class Minimax implements MoveFinder {
    private final int depth;
    private final PlayerType player; // evaluate for this player specifically
    /**
     * Constructor.
     */
    Minimax(int depth, PlayerType player) {
      this.depth = depth;
      this.player = player;
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
        var score = this.minimax(board, this.depth - 1, opponent);
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
     */
    private int minimax(BoardModel board, int remainDepth, PlayerType currentPlayer) {
      boolean maximizer = (currentPlayer == this.player);
      var score = maximizer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
      var legalMoves = board.getAllLegalMoves(currentPlayer);
      if (legalMoves.isEmpty()) { // checkmate
        return score;
      }
      if (remainDepth == 0) {
        return evaluateBoard(board, this.player, legalMoves);
      }
      var nextPlayer = flipPlayer(currentPlayer);
      for (var move : board.getAllLegalMoves(currentPlayer)) {
        move.apply(board);
        var newScore = this.minimax(board, remainDepth - 1, nextPlayer);
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
    private final int initialDepth;
    private final PlayerType player; // evaluate for this player specifically
    private final TranspositionTable cache = new TranspositionTable();
    private int cacheHits;
    private int explored;
    /**
     * Constructor.
     */
    AlphaBeta(int initialDepth, PlayerType player) {
      this.initialDepth = initialDepth;
      this.player = player;
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

      this.cacheHits = 0;
      this.explored = 0;
      var start = System.nanoTime(); // profiling
      // do 1 step expansion here, since `alphabeta` returns score only.
      for (var move : legalMoves) {
        move.apply(board);
        var score = this.alphabeta(board, this.initialDepth - 1, bestScore, Integer.MAX_VALUE, opponent);
        System.out.printf("score = %d, bestScore = %d, move = %s, cache size = %d\n", 
            score, bestScore, move, this.cache.size());
        if (bestMove == null || score > bestScore) { // must be `>`
          bestMove = move;
          bestScore = score;
        }
        move.undo(board);
      }
      this.cache.clear(); // most entries won't be re-usable
      var end = System.nanoTime();
      System.out.printf("Took %.3fs, explored %d nodes, cache hits = %d\n",
          (end - start) / 1e9, explored, cacheHits);

      return bestMove;
    }

    /**
     * Evaluate given board based on the minimax algorithm with alpha-beta pruning.
     * **REQUIRES**: `lower` is smaller than `upper`
     * **ENSURES**:
     * - if return value ∈ (lower, upper) or `remainDepth` == 0, or checkmate, then it's exact
     * - else it's either an upper/lower bound of the exact score, depending on minimizer/maximizer
     * @param board          is the current board state.
     * @param remainDepth    is how many more recursions to take.
     * @param lower          is the minimum score originalPlayer is guaranteed to achieve.
     * @param upper          is the maximum score originalPlayer can achieve.
     * @param currentPlayer  is the player to make next move.
     */
    private int alphabeta(BoardModel board, int remainDepth, int lower, int upper, PlayerType currentPlayer) {
      this.explored += 1;
      boolean maximizer = (currentPlayer == this.player);
      var score = maximizer ? Integer.MIN_VALUE : Integer.MAX_VALUE;
      var legalMoves = board.getAllLegalMoves(currentPlayer);
      if (legalMoves.isEmpty()) { // checkmate
        return score;
      }
      if (remainDepth == 0) {
        return evaluateBoard(board, this.player, legalMoves);
      }
      // check cache
      var entryOpt = this.cache.get(board, currentPlayer);
      if (entryOpt.isPresent() && entryOpt.get().depth >= remainDepth) {
        cacheHits += 1;
        var entry = entryOpt.get();
        switch (entry.type) {
          case EXACT: return entry.score;
          // more accurate upper/lower bounds
          case UPPER: upper = Integer.min(upper, entry.score);
                      break;
          case LOWER: lower = Integer.max(lower, entry.score);
                      break;
        }
        if (lower >= upper) {
          return entry.score;
        }
      }

      var nextPlayer = flipPlayer(currentPlayer);
      // maximizer keeps pushing up lower bound, and opposite for minimizer.
      for (var move : legalMoves) {
        move.apply(board);
        var newScore = this.alphabeta(board, remainDepth - 1, lower, upper, nextPlayer);
        move.undo(board);
        score = maximizer ?
          Integer.max(score, newScore) :
          Integer.min(score, newScore);
        if (maximizer) {
          lower = Integer.max(lower, score);
        } else {
          upper = Integer.min(upper, score);
        }
        // no need to continue further, since this branch won't make things better for either player
        // e.g. `this.player` can already ensure a score = 10, if opponent won't let it get
        // anything better than 10, then what's the point of continuing in this branch?
        if (lower >= upper) {
          break;
        }
      }
      // store result in cache
      EntryType type = EntryType.EXACT;
      if (score <= lower) {
        type = EntryType.UPPER;
      } else if (score >= upper) {
        type = EntryType.LOWER;
      }
      this.cache.put(board.getCopy(), currentPlayer, score, remainDepth, type);
      return score;
    }
  }

  /**
   * Evaluate the state of `board` for `player`.
   * Higher score means better chance of winning.
   */
  static private int evaluateBoard(BoardModel board, PlayerType player, Collection<Move> legalMoves) {
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
    score += legalMoves.size();
    return score;
  }
}
