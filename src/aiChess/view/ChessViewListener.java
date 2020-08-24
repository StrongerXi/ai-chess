package aiChess.view;
import aiChess.model.PlayerType;

/**
 * A callback interface used to handle user input received within the ChessView.
 */
public interface ChessViewListener {

  /**
   * The agent that controls a (top/bottom) player.
   */
  enum PlayerAgent {
    HUMAN, EASY_COMPUTER, MEDIUM_COMPUTER, HARD_COMPUTER
  }

  /**
   * Make appropriate response to user's request to use `agent` for
   * controlling `player`.
   */
  void setPlayerAgentRequested(PlayerType player, PlayerAgent agent);

  
  /**
   * Make appropriate response when the user has requested to make a move
   * from (srow, scol) to (drow, dcol).
   * @param srow the row index of source tile
   * @param scol the col index of source tile
   * @param trow the row index of target tile
   * @param tcol the col index of target tile
   */
  void moveRequested(int srow, int scol, int trow, int tcol);


  /**
   * Make appropriate response to the user's undo request.
   */
  void undoRequested();
}
