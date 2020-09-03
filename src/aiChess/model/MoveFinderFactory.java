package aiChess.model;

import aiChess.model.TranspositionTable.EntryType;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * A factory to generate different types of MoveFinder.
 */
public final class MoveFinderFactory {

  public enum MoveFinderType {
    MINIMAX, ALPHA_BETA, MTDF
  }

  /* Prevent instantiation */
  private MoveFinderFactory() {}

  /**
   * Generate a MoveFinder instance for `player`, based on `type`, with `depth` being
   * maximum search depth.
   */
  public static MoveFinder makeMoveFinder(MoveFinderType type, int depth, PlayerType player) {
    switch (type) {
      case MINIMAX:    return new Minimax(depth, player);
      case ALPHA_BETA: return new AlphaBeta(depth, player);
      case MTDF:       return new IterativeMtdf(depth, player);
      default:
        throw new RuntimeException("Unsupported MoveFinderType: " + type.toString());
    }
  }

  // Using Integer.(MAX/MIN)_VALUE isn't computation friendly
  // This must be larger than any possible return value of `evaluateBoard`
  private static final int MAX_SCORE = 1000000;
  private static final int MIN_SCORE = -MAX_SCORE;

  /**
   * MoveFinder based on the naive minimax search algorithm.
   */
  private static final class Minimax implements MoveFinder {
    private final int depth;
    private final PlayerType player; // evaluate for this player specifically
    private final TranspositionTable cache = new TranspositionTable();
    // maps remainDepth to cache hits
    private Map<Integer, Integer> cacheHits = new TreeMap<>();
    private int explored;
    private int expanded; // # of nodes that invoked `getAllLegalMoves`
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
      var bestScore = MIN_SCORE;
      var opponent = player.getOpponent();

      this.cacheHits.clear();
      this.explored = 0;
      this.expanded = 0;
      var start = System.nanoTime(); // profiling
      // do 1 step expansion here, since `minimax` returns score only.
      for (var move : legalMoves) {
        move.apply(board);
        var score = this.minimax(board, this.depth - 1, opponent);
        System.out.printf("score = %d, bestScore = %d, move = %s, cache size = %d\n",
            score, bestScore, move, this.cache.size());
        if (score >= bestScore) { // == in case all moves end in checkmate
          bestMove = move;
          bestScore = score;
        }
        move.undo(board);
      }
      this.cache.clear(); // most entries won't be re-usable
      var end = System.nanoTime();
      System.out.printf("Took %.3fs, nodes explored = %d, expanded = %d\n",
          (end - start) / 1e9, explored, expanded);
      for (var entry : this.cacheHits.entrySet()) {
        var depth = entry.getKey();
        var hits  = entry.getValue();
        System.out.printf("At depth %d, cache hits = %d\n", depth, hits);
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
      this.explored += 1;
      boolean maximizer = (currentPlayer == this.player);
      var entryOpt = this.cache.get(board, currentPlayer);
      if (entryOpt.isPresent() && entryOpt.get().depth >= remainDepth) {
        cacheHits.merge(remainDepth, 1, Integer::sum);
        assert(entryOpt.get().type == EntryType.EXACT);
        return entryOpt.get().score;
      }
      var score = maximizer ? MIN_SCORE : MAX_SCORE;
      var legalMoves = board.getAllLegalMoves(currentPlayer);
      var nextPlayer = currentPlayer.getOpponent();
      if (legalMoves.isEmpty()) { // checkmate
        return score;
      }
      this.expanded += 1;
      if (remainDepth == 0) {
        legalMoves.addAll(board.getAllLegalMoves(nextPlayer));
        score = evaluateBoard(board, this.player, legalMoves);
      } else {
        for (var move : board.getAllLegalMoves(currentPlayer)) {
          move.apply(board);
          var newScore = this.minimax(board, remainDepth - 1, nextPlayer);
          score = maximizer ?
            Integer.max(score, newScore) :
            Integer.min(score, newScore);
          move.undo(board);
        }
      }
      this.cache.put(board.getCopy(), currentPlayer, score, remainDepth, EntryType.EXACT);
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
    // maps remainDepth to cache hits
    private Map<Integer, Integer> cacheHits = new TreeMap<>();
    private int explored;
    private int expanded; // # of nodes that invoked `getAllLegalMoves`
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
      var bestScore = MIN_SCORE;
      var opponent = player.getOpponent();

      this.cacheHits.clear();
      this.explored = 0;
      this.expanded = 0;
      var start = System.nanoTime(); // profiling
      // do 1 step expansion here, since `alphabeta` returns score only.
      for (var move : legalMoves) {
        move.apply(board);
        var score = this.alphabeta(board, this.initialDepth - 1, bestScore, MAX_SCORE, opponent);
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
      System.out.printf("Took %.3fs, nodes explored = %d, expanded = %d\n",
          (end - start) / 1e9, explored, expanded);
      for (var entry : this.cacheHits.entrySet()) {
        var depth = entry.getKey();
        var hits  = entry.getValue();
        System.out.printf("At depth %d, cache hits = %d\n", depth, hits);
      }

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
      var score = maximizer ? MIN_SCORE : MAX_SCORE;
      // check cache
      var entryOpt = this.cache.get(board, currentPlayer);
      if (entryOpt.isPresent() && entryOpt.get().depth >= remainDepth) {
        cacheHits.merge(remainDepth, 1, Integer::sum);
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
      var legalMoves = board.getAllLegalMoves(currentPlayer);
      var nextPlayer = currentPlayer.getOpponent();
      if (legalMoves.isEmpty()) { // checkmate
        return score;
      }
      if (remainDepth == 0) {
        legalMoves.addAll(board.getAllLegalMoves(nextPlayer));
        score = evaluateBoard(board, this.player, legalMoves);
        this.cache.put(board.getCopy(), currentPlayer, score, 0, EntryType.EXACT);
        return score;
      }
      this.expanded += 1;
      final int originalLower = lower, originalUpper = upper;
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
      if (score <= originalLower) {
        type = EntryType.UPPER;
      } else if (score >= originalUpper) {
        type = EntryType.LOWER;
      }
      this.cache.put(board.getCopy(), currentPlayer, score, remainDepth, type);
      return score;
    }
  }

  private static final class IterativeMtdf implements MoveFinder {
    private final int maxDepth;
    private final AlphaBeta abFinder;
    /**
     * Constructor.
     */
    IterativeMtdf(int maxDepth, PlayerType player) {
      this.maxDepth = maxDepth;
      // `maxDepth` is technically never used in `abFinder`
      this.abFinder = new AlphaBeta(maxDepth, player);
    }

    @Override
    public Move getBestMove(ChessGameModel model) {
      var player = model.getCurrentPlayer();
      var board = model.getBoardCopy();
      var legalMoves = board.getAllLegalMoves(player);
      assert(!legalMoves.isEmpty());

      // find the move with best score
      Move bestMove = null;
      var bestScore = MIN_SCORE;
      var opponent = player.getOpponent();

      this.abFinder.cacheHits.clear();
      this.abFinder.explored = 0;
      var start = System.nanoTime(); // profiling
      // do 1 step expansion here, since `alphabeta` returns score only.
      for (var move : legalMoves) {
        move.apply(board);
        var score = this.mtdf(board, bestScore, this.maxDepth - 1, opponent);
        System.out.printf("score = %d, bestScore = %d, move = %s, cache size = %d\n",
            score, bestScore, move, this.abFinder.cache.size());
        if (bestMove == null || score > bestScore) { // must be `>`
          bestMove = move;
          bestScore = score;
        }
        move.undo(board);
      }
      this.abFinder.cache.clear(); // most entries won't be re-usable
      var end = System.nanoTime();
      System.out.printf("Took %.3fs, nodes explored = %d, expanded = %d\n",
          (end - start) / 1e9, this.abFinder.explored, this.abFinder.expanded);
      for (var entry : this.abFinder.cacheHits.entrySet()) {
        var depth = entry.getKey();
        var hits  = entry.getValue();
        System.out.printf("At depth %d, cache hits = %d\n", depth, hits);
      }
      return bestMove;
    }

    private int mtdf(BoardModel board, int bestScore, int initDepth, PlayerType initPlayer) {
      int score;
      int scoreUpper = MAX_SCORE;
      int scoreLower = bestScore;
      int count = 0;
      do {
        //int oldExplored = this.abFinder.explored;
        int windowUpper = Math.floorDiv(scoreUpper + scoreLower, 2) + 1;
        score = this.abFinder.alphabeta(board, initDepth, windowUpper - 1, windowUpper, initPlayer);
        if (score < windowUpper) {
          scoreUpper = score;
        } else {
          scoreLower = score;
        }
        count += 1;
        //int explored = this.abFinder.explored - oldExplored;
        //System.out.printf("[mtdf] explored %d nodes in 1 iteration, lower = %d, upper = %d, window = %d\n",
        //    explored, scoreLower, scoreUpper, windowUpper);
        //this.abFinder.cache.clear();
      } while (scoreLower < scoreUpper);
      //System.out.printf("[mtdf] %d iterations.\n", count);
      return score;
    }
  }

  static private int pieceMaterialScore(PieceType type) {
    switch(type) {
      case PAWN:   return 10;
      case KNIGHT: return 30;
      case BISHOP: return 30;
      case CASTLE: return 50;
      case QUEEN:  return 90;
      case KING:   return 900;
      default:
        throw new RuntimeException("Unsupported piece type: " + type.toString());
    }
  }

  /**
   * Evaluate the state of `board` for `player`.
   * Higher score means better chance of winning.
   * @param legalMoves are the legal moves for the both players.
   */
  static private int evaluateBoard(BoardModel board, PlayerType player, Collection<Move> legalMoves) {
    int materialScore = 0;
    int forwardOffset = (player == PlayerType.TOP_PLAYER) ? -1 : 1;
    int startRow      = (player == PlayerType.TOP_PLAYER) ? board.height-1 : 0;
    // material and position related scores
    for (int row = 0; row < board.height; row += 1) {
      for (int col = 0; col < board.width; col += 1) {
        var pieceOpt = board.getPieceAt(row, col);
        if (pieceOpt.isEmpty()) {
          continue;
        }
        var piece = pieceOpt.get();
        var pieceScore = pieceMaterialScore(piece.type);
        if (piece.type == PieceType.PAWN) {
          // closer to border -> higher chance of promotion
          pieceScore += Math.abs(row - startRow);
          var forwardRow = row + forwardOffset;
          if (forwardRow < 0 || forwardRow >= board.height) {
            continue;
          }
          var forwardPieceOpt = board.getPieceAt(forwardRow, col);
          if (forwardPieceOpt.isPresent()) { // blocked pawn
            pieceScore += 5;
            var forwardPiece = forwardPieceOpt.get();
            if (forwardPiece.type == PieceType.PAWN &&
                forwardPiece.owner == piece.owner) { // doubled pawn
              pieceScore += 5;
            }
          }
        }
        if (player != piece.owner) {
          pieceScore *= -1;
        }
        materialScore += pieceScore;
      }
    }
    // move related scores
    var moveScore = 0;
    for (var move : legalMoves) {
      var score = 1; // mobility bonus
      var srcPos = move.sourcePos;
      var source = board.getPieceAt(srcPos.row, srcPos.col).get();
      /* TODO this is too naive, leading AI to attack blindly
      var dstPos = move.targetPos;
      var targetOpt = board.getPieceAt(dstPos.row, dstPos.col);
      if (targetOpt.isPresent()) {
        // protect or attack
        var target = targetOpt.get();
        // not exactly the same as actually attacked/protected
        var pieceScore = pieceMaterialScore(target.type) / 2;
        score += pieceScore;
      }
      */
      if (source.owner != player) {
        score *= -1;
      }
      moveScore += score;
    }
    return materialScore + moveScore;
  }
}
