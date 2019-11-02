package aiChess.view;

import aiChess.model.ChessGameModel;

/**
 * General View interface for a Chess Game.
 * It must support the following feature:
 * - Allows client to set model and listener.
 * - Render the current model game to user (model, whose turn, etc.).
 * - Pass high level user request to listener.
 * - display prompt/error message.
 */
public interface ChessView {

  /**
   * Use given model as the source of game state info.
   */
  void setModel(ChessGameModel model);


  /**
   * Use given listener for processing user requests.
   */
  void setListener(ChessViewListener listener);


  /**
   * Render the model and allow View to start receive input from user.
   */
  void beginInteraction();


  /**
   * Display the given error message to user in an informative way.
   * @param error the error message to be shown
   */
  void showMessage(String error);


  /**
   * Refresh the output and reflect most updated state of model.
   */
  void refresh();
}
