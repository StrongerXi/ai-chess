package aiChess.control;

import aiChess.view.ChessViewListener;
import aiChess.view.ChessView;
import aiChess.view.GameOverOption;
import aiChess.model.ChessGameModel;
import aiChess.model.PlayerType;
import aiChess.model.error.InvalidUndoException;
import aiChess.model.error.InvalidMoveException;

import java.util.Objects;


/**
 * A controller that mediates interaction between a Chess Model and View.
 */
public class ChessController implements ChessViewListener {

  /**
   * Who is controlling the action of a player.
   */
  public static enum PlayerController {
    USER, AI
  }

  private final ChessGameModel model;
  private final ChessView view;
  private final PlayerController topController = PlayerController.USER;
  private final PlayerController botController = PlayerController.USER;

  /**
   * Constructor.
   */
  public ChessController(ChessGameModel model, ChessView view) {
    Objects.requireNonNull(model, "model is null\n");
    Objects.requireNonNull(view, "view is null\n");
    this.model = model;
    this.view = view;
  }

  /**
   * Start running the application.
   */
  public void run() {
    new Thread(() -> {
      this.view.setModel(model);
      this.view.setListener(this);
      this.view.beginInteraction();
    }).start();

    while (true) {
      if (!this.model.isGameOver()) { // unlikely but well...
        while (!this.takeTurn(model.getCurrentPlayer()));
      }
      // game is over
      var player = this.model.getCurrentPlayer();
      var option = this.view.gameOverPrompt(player);
      // act based on user response
      switch (option) {
        case RESTART: {
          this.view.showMessage("Restarting...\n");
          this.model.restart();
          continue;
        }
        case QUIT: {
          this.view.showMessage("Quitting...\n");
          this.view.stopInteraction();
          return;
        }
      }
    }
  }

  // User uses View to select and request a move
  // But AI player can't. To support an execution model (simplified):
  // - player1.takeTurn()
  // - player2.takeTurn()
  // - repeat
  // We need to make `takeTurn` a blocking call that potentially depends on
  // User input from View, which is asynchronous.
  //
  // Meanwhile we might want to handle events not related to `takeTurn`, e.g.
  // save/load game, which just requires a lock to the model.
  //
  // The following variables enable asynchronous user action from View to unblock
  // the `takeTurn` method.
  private volatile boolean playerActed = false;
  private final Object lock = new Object();

  /**
   * Based on given control, take an action.
   * REQUIRES: this.model.isGameOver == false
   * ENSURES:  Upon normal exit, this.model.currentPlayer() changes.
   * @return whether the game is over
   */
  private boolean takeTurn(PlayerType player) {
    var control = (player == PlayerType.TOP_PLAYER) ? topController : botController;
    // TODO anti-OOD, but the logic is simple enough to be pragmatic for now.
    switch (control) {
      case AI: { 
        // TODO dumb AI relies on human.
      }
      case USER: {
        synchronized (lock) {
          // wait until one of the *Requested methods signals.
          while(!this.playerActed) { // prevent spurious wakeup
            try {
              lock.wait();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          this.playerActed = false;
        }
      }
    }
    return this.model.isGameOver();
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
      synchronized (lock) {
        this.model.makeMove(srow, scol, trow, tcol);
        playerActed = true;
        lock.notify();
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
      synchronized (lock) {
        this.model.undoLastMove();
        playerActed = true;
        lock.notify();
      }
    } catch (InvalidUndoException e) {
      this.view.showMessage(e.getMessage());
    }
  }
}
