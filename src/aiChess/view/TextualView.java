package aiChess.view;

import aiChess.model.ChessGameModel;
import aiChess.model.Piece;
import aiChess.model.PieceType;
import aiChess.model.PlayerType;
import aiChess.model.Position;

import java.util.Arrays;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.InputMismatchException;

import java.io.IOException;
import java.lang.NumberFormatException;


/**
 * A chess view that displays the game and interacts with client via String.
 */
public class TextualView implements ChessView {


  /* used to represent the state of a tile, for different illustration purposes. */
  private enum TileState {
    ATTACKABLE, /* Occupied tile that can be attacked by the selected piece */
    REACHABLE, /* Empty tile that can be reached by the selected piece */
    NORMAL, /* A normal tile, either occupied or empty */
    SELECTED/* The currently selected tile (should be occupied) */
  }



  private static final char[] SELECTED_BAR = {'{', '}'};
  private static final char[] ATTACKABLE_BAR = {'(', ')'};
  private static final char[] REACHABLE_BAR = {'[', ']'};

  /* Used to represent the default space surrounding each Piece */
  private static final char[] DEFAULT_BAR = {'|', '|'};
  private static final char EMPTY_SYMBOL = ' ';

  /**
   * Returns the index that actually represents the column index of the single char 
   * that represents the index-th Piece on any row.
   * @param index is the conceptual index that refers to the index-th Piece on a row
   * @return the index that corresponds to the char for the specified Piece in this.board.
   */
  private static int trueColIndex(int index) {
    return 1 + 3 * index;
  }


  /**
   * Return the lower-case character that will represents the Piece of given PieceType.
   */
  private static char pieceTypeToChar(PieceType type) {
    switch (type) {
      case KING: 
        return 'k';
      case QUEEN:
        return 'q';
      case BISHOP:
        return 'b';
      case KNIGHT:
        return 'n';
      case CASTLE:
        return 'c';
      case PAWN:
        return 'p';
      default:
        throw new IllegalArgumentException("PieceType not recognized\n");
    }
  }


  /*
   * For ease of converting to a String, a 2-d array of char
   * is used to store the information for displaying each Pieces.
   * Each Piece is represented with a single character, with upper-player's Pieces capitalized.
   * Each character is surrounded by 2 spaces, for inserting background information.
   *
   * For instance:
   * K  Q  B  B (N)
   * P (P)(P) -
   *    c [q]
   *
   * Therefore, for a board with X Pieces in a row, there are actually 3 * X characters.
   */
  private int width = 0, height = 0;
  private char[][] board;
  private final Appendable output;
  private Optional<ChessViewListener> listener = Optional.empty();
  private Optional<ChessGameModel> model = Optional.empty();
  private Scanner scan;
  private boolean running = false;

  /**
   * Initiate the Viewer with a board of given configuration.
   * @param width number of Pieces to be shown in a row
   * @param height number of Pieces to be shown in a column
   * @throws IllegalArgumentException if either width/height is non-positive, or player1 == player2
   */
  public TextualView(Readable input, Appendable output) {
    if (input == null || output == null) {
      throw new IllegalArgumentException("Cannot null inputs\n");
    }

    this.scan = new Scanner(input);
    this.output = output;
  }


  /*
   * Change the char surrounding the char for Piece at (row, col) on Chess Board
   * based on the given TileState.
   */
  private void setBackgroundAt(int row, int col, TileState state) {

    col = trueColIndex(col);
    char[] bars;
    switch(state) {
      case REACHABLE: bars = REACHABLE_BAR;
                      break;
      case ATTACKABLE: bars = ATTACKABLE_BAR;
                      break;
      case SELECTED: bars = SELECTED_BAR;
                     break;
      case NORMAL: bars = DEFAULT_BAR;
                   break;
      default:
        throw new IllegalArgumentException("TileState not recognized\n");
    }

    board[row][col - 1] = bars[0];
    board[row][col + 1] = bars[1];
  }



  /**
   * Print out the message to Standard out.
   */
  @Override
  public void showMessage(String msg) {
    System.out.printf(msg);
  }


  /* the position that was selected in the last input (r, c) */
  private Optional<Position> lastSelected = Optional.empty();

  /**
   * Start the while loop and take in user input.
   * Invoke user action handler accordingly.
   */
  @Override
  public void beginInteraction() {
    this.running = true;
    this.display();
    model.ifPresent(model -> {
      while (this.running) {
        try {
          String token = scan.next();
          if (token.equals("undo")) {
            this.listener.ifPresent(l -> l.undoRequested());
            this.lastSelected = Optional.empty();
            this.synchWithModel(model);
            continue;
          } 

          int row = Integer.parseInt(token);
          int col = scan.nextInt();
          if (!this.isValidPos(row, col)) {
            this.showMessage("Position is invalid\n");
            continue;
          }

          /* if there was a selection previously, this selection indicates a move request */
          if (lastSelected.isPresent()) {
            int srow = lastSelected.get().row;
            int scol = lastSelected.get().col;
            /* synch with model again to get rid of the background highlights 
             * from previous selection, note that move may fail.*/
            this.synchWithModel(model);
            this.listener.ifPresent(l -> l.moveRequested(srow, scol, row, col));
            /* synch again after move */
            this.synchWithModel(model);
            lastSelected = Optional.empty();

          } else {
            /* ignore if selected tile is empty or it's not this player's turn yet */
            Optional<Piece> source = model.getPieceAt(row, col);
            if (!source.isPresent() || source.get().owner != model.getCurrentPlayer()) {
              this.showMessage("Source is empty or not this player's turn\n");
              continue;
            }

            this.setBackgroundAt(row, col, TileState.SELECTED);
            /* light up all reachable states */
            for (Position pos : model.getAllMovesFrom(row, col)) {
              TileState state = model.getPieceAt(pos.row, pos.col).isPresent() ? 
                TileState.ATTACKABLE : TileState.REACHABLE;
              this.setBackgroundAt(pos.row, pos.col, state);
            }
            lastSelected = Optional.of(Position.of(row, col));
            display(); // display highlighted tiles
          }

        } catch (InputMismatchException e) {
          this.showMessage("Please specify either numerical input or 'undo'");

        } catch (NumberFormatException e) {
          this.showMessage("Please specify a valid number input");
        }
      }
    });
  }

  /**
   * Return true iff (row, col) is within bounds of current model.
   */
  private boolean isValidPos(int row, int col) {
    return (0 <= row && row < height && 0 <= col && col < width);
  }


  /**
   * Print out the board state to Standard out as String;
   * each row will be ended with a newline '\n' character, including the last row.
   */
  private void display() {

    StringBuilder sb = new StringBuilder();
    for (char[] row : this.board) {
      for (char ch : row) {
        sb.append(ch);
      }
      /* Add the newline character at the end */
      sb.append('\n');
    }

    try {
      output.append(sb.toString());
    } catch (IOException e) {
      throw new IllegalStateException("Output IO error\n");
    }
  }

  /**
   * Synchronize the board display content with data from given model.
   * Without printing it out.
   */
  private void synchWithModel(ChessGameModel model) {
    for (int row = 0; row < this.height; row += 1) {
      for (int col = 0; col < this.width; col += 1) {
        /* update the char for position (row, col) */
        Optional<Piece> piece = model.getPieceAt(row, col);
        char ch = EMPTY_SYMBOL;
        /* set ch to reflect the specific piece at (row, col) */
        if (piece.isPresent()) {
          ch = pieceTypeToChar(piece.get().type);
          if (piece.get().owner == PlayerType.TOP_PLAYER) {
            ch = Character.toUpperCase(ch);
          }
        }
        this.setBackgroundAt(row, col, TileState.NORMAL);
        this.board[row][trueColIndex(col)] = ch;
      }
    }
  }


  /**
   * Use given model as the source of game state info.
   */
  @Override
  public void setModel(ChessGameModel model) {
    this.model = Optional.ofNullable(model);
    this.height = model == null ? 0 : model.getHeight();
    this.width = model == null ? 0 : model.getWidth();

    this.board = new char[height][];

    int rowLength = trueColIndex(width);
    for (int row = 0; row < height; row += 1) {
      board[row] = new char[rowLength];
    }
    this.model.ifPresent(this::synchWithModel);
  }


  /**
   * Refresh the output and reflect most updated state of model.
   */
  @Override
  public void refresh() {
    this.model.ifPresent(model -> {
      this.synchWithModel(model);
      this.display();
    });
  }


  /**
   * Use given listener as listener of user requests.
   */
  @Override
  public void setListener(ChessViewListener listener) {
    this.listener = Optional.ofNullable(listener);
  }


  @Override
  public GameOverOption gameOverPrompt(PlayerType player) {
    var winner = (player == PlayerType.TOP_PLAYER) ? "<top>" : "<bottom>";
    this.showMessage("The game is over, player " + winner + " won\n");
    this.showMessage("Enter 'r' to restart, else to quit.\n");
    String token = this.scan.next();
    switch (token) {
      case "r": return GameOverOption.RESTART;
      default : return GameOverOption.QUIT;
    }
  }


  @Override
  public void stopInteraction() {
    this.running = false;
  }
}
