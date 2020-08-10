package aiChess.control;

import aiChess.view.ChessViewListener;
import aiChess.view.ChessView;
import aiChess.view.GameOverOption;
import aiChess.model.ChessGameModel;
import aiChess.model.error.InvalidUndoException;
import aiChess.model.error.InvalidMoveException;

import java.util.Objects;


/**
 * A controller that mediates interaction between a Chess Model and View.
 */
public class ChessController implements ChessViewListener {

  private final ChessGameModel model;
  private final ChessView view;

  public ChessController(ChessGameModel model, ChessView view) {
    Objects.requireNonNull(model, "model is null\n");
    Objects.requireNonNull(view, "view is null\n");
    this.model = model;
    this.view = view;
    view.setModel(model);
    view.setListener(this);
    this.view.beginInteraction();
  }


  /**
   * Make appropriate response when the user has requested to make a move
   * from (srow, scol) to (drow, dcol).
   * @param srow the row index of source tile
   * @param scol the col index of source tile
   * @param trow the row index of target tile
   * @param tcol the col index of target tile
   */
  public void moveRequested(int srow, int scol, int trow, int tcol) {
    try {
      var player = this.model.getCurrentPlayer();
      this.model.makeMove(srow, scol, trow, tcol);
      if (this.model.isGameOver()) {
        this.view.refresh();
        var option = this.view.gameOverPrompt(player);
        switch (option) {
          case RESTART: {
            this.view.showMessage("Restarting...\n");
            this.model.restart();
            return;
          }
          case QUIT: {
            this.view.showMessage("Quitting...\n");
            this.view.stopInteraction();
            return;
          }
          default:
            throw new RuntimeException("Unknown game over option: " + option.toString());
        }
      }
    } catch (InvalidMoveException e) {
      this.view.showMessage(e.getMessage());
    }
  }


  /**
   * Make appropriate response to the user's undo request.
   */
  public void undoRequested() {
    try {
      this.model.undoLastMove();

    } catch (InvalidUndoException e) {
      this.view.showMessage(e.getMessage());
    }
  }
}
