package aiChess.model;

/**
 * An interface for finding optimal move for current player in a game.
 */
public interface MoveFinder {
  /**
   * Return the best move for current player in `model`.
   * ASSUME model.isGameOver() == false
   */
  Move getBestMove(ChessGameModel model);
}
