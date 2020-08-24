package aiChess.control;

import aiChess.view.ChessViewListener;
import aiChess.view.ChessViewListener.PlayerAgent;
import aiChess.view.ChessView;
import aiChess.view.GameOverOption;
import aiChess.model.ChessGameModel;
import aiChess.model.MoveFinder;
import aiChess.model.MoveFinderFactory;
import aiChess.model.PlayerType;
import aiChess.model.MoveFinderFactory.MoveFinderType;
import aiChess.model.error.InvalidUndoException;
import aiChess.model.error.InvalidMoveException;

import java.util.Objects;
import java.util.Map;
import java.util.HashMap;


/**
 * A controller that mediates interaction between a Chess Model and View.
 */
public class ChessController implements ChessViewListener {

  /**
   * Who/What is controlling the action of a player. Essentially a closure.
   * ASSUME synchronization is taken care of before using this closure.
   */
  private static interface PlayerController {
    /**
     * Make 1 move.
     */
    void makeMove();
  }

  // Controlled by user via UI
  private final class UserController implements PlayerController {
    public void makeMove() {
      // wait until the play is completed via view UI
      while(!ChessController.this.playerActed) { // prevent spurious wakeup
        try {
          ChessController.this.lock.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      ChessController.this.playerActed = false;
    }
  }

  // Controlled by computer (i.e. MoveFinder)
  private final static class AIController implements PlayerController {
    private final MoveFinder finder;
    private final ChessGameModel model;
    private final ChessView view;
    AIController(MoveFinder finder, ChessGameModel model, ChessView view) {
      this.finder = finder;
      this.model = model;
      this.view = view;
    }
    public void makeMove() {
      var move = this.finder.getBestMove(this.model);
      var src = move.sourcePos;
      var dst = move.targetPos;
      this.model.makeMove(src.row, src.col, dst.row, dst.col);
      this.view.refresh(); // this move bypassed view, manually sync it.
    }
  }

  private final ChessGameModel model;
  private final ChessView view;
  // only makes sense if corresponding controller is AI
  private final Map<PlayerType, MoveFinder> moveFinderMap = new HashMap<>();
  private final Map<PlayerType, PlayerController> controllerMap = new HashMap<>();


  /**
   * Constructor.
   */
  public ChessController(ChessGameModel model, ChessView view) {
    Objects.requireNonNull(model, "model is null\n");
    Objects.requireNonNull(view, "view is null\n");
    this.model = model;
    this.view = view;
    var defaultFinder =
      MoveFinderFactory.makeMoveFinder(MoveFinderType.MTDF, 4, PlayerType.TOP_PLAYER);
    var userControl = new UserController();
    var aiControl   = new AIController(defaultFinder, this.model, this.view);
    // default user vs computer
    this.controllerMap.put(PlayerType.TOP_PLAYER, aiControl);
    this.controllerMap.put(PlayerType.BOTTOM_PLAYER, userControl);
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
   * Let given player take an action.
   * REQUIRES: this.model.isGameOver == false
   * ENSURES:  Upon normal exit, this.model.currentPlayer() changes.
   * @return whether the game is over
   */
  private boolean takeTurn(PlayerType player) {
    var control = this.controllerMap.get(player);
    synchronized (lock) {
      control.makeMove();
    }
    return this.model.isGameOver();
  }

  // Interpretation of `PlayerAgent` lies here, especially for different levels
  // of computer agents.
  public void setPlayerAgentRequested(PlayerType player, PlayerAgent agent) {
    PlayerController controller = new UserController(); // default to human
    if (agent != PlayerAgent.HUMAN) {
      MoveFinder finder;
      switch (agent) {
        case EASY_COMPUTER: {
          finder = MoveFinderFactory.makeMoveFinder(MoveFinderType.MINIMAX, 2, player);
          break;
        }
        case MEDIUM_COMPUTER: {
          finder = MoveFinderFactory.makeMoveFinder(MoveFinderType.ALPHA_BETA, 3, player);
          break;
        }
        default: { // HARD_COMPUTER
          finder = MoveFinderFactory.makeMoveFinder(MoveFinderType.MTDF, 4, player);
        }
      }
      controller = new AIController(finder, this.model, this.view);
    }
    this.controllerMap.put(player, controller);
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
        var player = this.model.getCurrentPlayer();
        var opponent = (player == PlayerType.TOP_PLAYER) ?
                        PlayerType.BOTTOM_PLAYER : PlayerType.TOP_PLAYER;
        // Since AI algorithms are currently (and likely in future) deterministic,
        // we don't support undo when both players are controlled by computers
        if (this.controllerMap.get(opponent) instanceof UserController) {
          this.model.undoLastMove(); // undo player move
        } else if (this.controllerMap.get(player) instanceof UserController) {
          this.model.undoLastMove(); // undo computer move
          this.model.undoLastMove(); // undo player move
        }
        playerActed = true;
        lock.notify();
      }
    } catch (InvalidUndoException e) {
      this.view.showMessage(e.getMessage());
    }
  }
}
