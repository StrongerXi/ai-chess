package aiChess.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Color;
import java.awt.Insets;

import javax.swing.border.EmptyBorder;

import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.net.URL;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

import aiChess.model.Piece;
import aiChess.model.PlayerType;
import aiChess.model.Position;
import aiChess.model.ChessGameModel;
import aiChess.view.ChessViewListener.PlayerAgent;


public class SwingView implements ChessView {

  static final int TILE_SIZE = 64;
  /**
   * Lazily initialized. First dimension is color, second is piece type.
   */
  static private ImageIcon[][] pieceImages = null;
  /**
   * Return the imageIcon associated with given piece.
   */
  static ImageIcon getImageIcon(Piece p) {
    if (pieceImages == null) {
      pieceImages = new ImageIcon[2][6];
      String[] names = {"pawn", "castle", "bishop", "queen", "king", "knight"};

      for (int i = 0; i < 6; i += 1) {
        var topIcon = new ImageIcon(SwingView.class.getResource("/top-" + names[i] + ".png"));
        var botIcon = new ImageIcon(SwingView.class.getResource("/bottom-" + names[i] + ".png"));
        pieceImages[0][i] = new ImageIcon(
            topIcon.getImage().getScaledInstance(TILE_SIZE, TILE_SIZE, Image.SCALE_SMOOTH));
        pieceImages[1][i] = new ImageIcon(
            botIcon.getImage().getScaledInstance(TILE_SIZE, TILE_SIZE, Image.SCALE_SMOOTH));
      }
    }
    var player = (p.owner == PlayerType.TOP_PLAYER) ? 0 : 1;
    var type = 0;
    switch (p.type) {
      case PAWN:   type = 0; break;
      case CASTLE: type = 1; break;
      case BISHOP: type = 2; break;
      case QUEEN:  type = 3; break;
      case KING:   type = 4; break;
      case KNIGHT: type = 5; break;
      default: throw new RuntimeException("Unsupported piece: " + p.type.toString());
    }
    return pieceImages[player][type];
  }

  /* used to represent the state of a tile, for different illustration purposes. */
  private static enum TileState {
    ATTACKABLE, // Occupied tile that can be attacked by the selected piece
    REACHABLE,  // Empty tile that can be reached by the selected piece
    NORMAL,     // A normal tile, either occupied or empty
    SELECTED    // The currently selected tile (should be occupied)
  }

  // UI panel for interactions unrelated to board
  private class UIPanel extends JPanel{

    private final String[] options = new String[]{ "human", "easy-ai", "medium-ai", "hard-ai" };
    private final PlayerAgent[] agents = new PlayerAgent[]{
      PlayerAgent.HUMAN, PlayerAgent.EASY_COMPUTER, PlayerAgent.MEDIUM_COMPUTER, PlayerAgent.HARD_COMPUTER };
    private final JComboBox<String> topBox = new JComboBox<>(options);
    private final JComboBox<String> botBox = new JComboBox<>(options);

    UIPanel() {
      super();
      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      var topPanel = this.createPlayerPanel(PlayerType.TOP_PLAYER, topBox);
      var botPanel = this.createPlayerPanel(PlayerType.BOTTOM_PLAYER, botBox);
      this.add(topPanel);
      this.add(botPanel);
    }

    // create a row panel for selecting
    private JPanel createPlayerPanel(PlayerType player, JComboBox<String> agentOpts) {
      var panel = new JPanel(new GridLayout());
      var changeButton = new JButton("change");
      changeButton.addActionListener(e -> {
        var index = agentOpts.getSelectedIndex();
        var agent = this.agents[index];
        SwingView.this.listener.ifPresent(l -> l.setPlayerAgentRequested(player, agent));
      });
      panel.add(agentOpts);
      panel.add(changeButton);
      var title = (player == PlayerType.TOP_PLAYER) ? "top" : "bottom";
      panel.setBorder(BorderFactory.createTitledBorder(title));
      return panel;
    }
  }

  private JFrame window;
  private JButton[][] boardButtons;
  private ChessGameModel model;
  private Optional<ChessViewListener> listener = Optional.empty();
  // selected by last button click event
  private Optional<Position> lastSelected = Optional.empty();

  /**
   * Constructor.
   */
  public SwingView(ChessGameModel model) {
    this.model = model;
    this.boardButtons = new JButton[model.getHeight()][];
    for (int row = 0; row < boardButtons.length; row += 1) {
      boardButtons[row] = new JButton[model.getWidth()];
    }
  }

  public void setModel(ChessGameModel model) {
    this.model = model;
  }

  public void setListener(ChessViewListener listener) {
    this.listener = Optional.of(listener);
  }

  public void beginInteraction() {

    Supplier<JToolBar> initToolBar = () -> {
      JToolBar toolbar = new JToolBar();
      toolbar.setFloatable(false);

      var restartButton = new JButton("Restart");
      var saveButton = new JButton("Save");
      var loadButton = new JButton("Load");
      var undoButton = new JButton("Undo");

      // TODO support these actions
      restartButton.addActionListener(e -> {});
      saveButton.addActionListener(e -> {});
      loadButton.addActionListener(e -> {});
      undoButton.addActionListener(e -> this.listener.ifPresent(l ->  {
        l.undoRequested();
        this.lastSelected = Optional.empty();
        this.refresh();
      }));

      toolbar.add(restartButton);
      toolbar.add(saveButton);
      toolbar.add(loadButton);
      toolbar.add(undoButton);
      // toolbar.addSeparator();
      return toolbar;
    };

    Supplier<JPanel> initBoardPanel = () -> {
      // each row has 8 items
      var boardPanel = new JPanel(new GridLayout(0, 8));
      boardPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
      boardPanel.setBackground(Color.ORANGE);

      // create the chess board squares, 
      // since (0, 0) is bottom left, we add top rows first.
      for (int row = model.getHeight() - 1; row >= 0; row -= 1) {
        for (int col = 0; col < model.getWidth(); col += 1) {
          JButton button = new JButton();
          // these 2 commands allow the buttons to be seamless and show background
          button.setOpaque(true);
          button.setBorderPainted(false);
          final int r = row, c = col; // to make lambda happy
          button.addActionListener(e -> this.buttonClicked(r, c));
          this.boardButtons[row][col] = button;
          boardPanel.add(button);
        }
      }
      this.refresh();
      return boardPanel;
    };

    javax.swing.SwingUtilities.invokeLater(() -> {
      var mainPanel = new JPanel(new BorderLayout());
      var toolbar = initToolBar.get();
      var boardPanel = initBoardPanel.get();
      var uiPanel = new UIPanel();

      mainPanel.add(toolbar, BorderLayout.NORTH);
      mainPanel.add(boardPanel, BorderLayout.CENTER);
      mainPanel.add(uiPanel, BorderLayout.EAST);

      this.window = new JFrame("Chess");
      window.add(mainPanel);
      window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      window.pack(); // Minimize frame size while displaying all components
      window.setMinimumSize(window.getSize()); // enforce the minimum size
      window.setResizable(false);
      window.setVisible(true);
    });
  }

  public void showMessage(String error) {
    JOptionPane.showMessageDialog(window, error);
  }

  public void refresh() {
    for (int row = 0; row < this.boardButtons.length; row += 1) {
      for (int col = 0; col < boardButtons[row].length; col += 1) {
        var piece = this.model.getPieceAt(row, col);
        var imageIcon = piece.isPresent() ? getImageIcon(piece.get()) : null;
        var button = boardButtons[row][col];
        button.setIcon(imageIcon);
        setBackgroundAt(row, col, TileState.NORMAL);
      }
    }
  }

  public GameOverOption gameOverPrompt(PlayerType winner) {
    String[] optStrs = {"restart", "quit"};
    GameOverOption[] options = {GameOverOption.RESTART, GameOverOption.QUIT};
    int index = JOptionPane.showOptionDialog(
        window,         // display in the `window` frame
        "Game is over", // prompt
        "Info",         // title
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.INFORMATION_MESSAGE,
        null,           // default icon
        optStrs,        // options to be selected
        null);          // no default selection
    return (index == JOptionPane.CLOSED_OPTION) ? 
           GameOverOption.QUIT : options[index];
  }

  public void stopInteraction() {
    this.window.dispose();
    this.window.setVisible(false);
    // TODO application won't terminate without explicitly exitting
    System.exit(0);
  }

  /**
   * Respond to a button click on the chess board.
   * ASSUME `row` and `col` are within range of `boardButtons`
   */
  private void buttonClicked(int row, int col) {
    var button = this.boardButtons[row][col];
    this.lastSelected.ifPresentOrElse(pos -> {
      int srow = pos.row;
      int scol = pos.col;
      this.listener.ifPresent(l -> l.moveRequested(srow, scol, row, col));
      this.refresh();
      this.lastSelected = Optional.empty();

    }, /* else */ () -> {
      Optional<Piece> source = model.getPieceAt(row, col);
      if (!source.isPresent() || source.get().owner != model.getCurrentPlayer()) {
        this.showMessage("Source is empty or not this player's turn\n");
        return;
      }
      this.setBackgroundAt(row, col, TileState.SELECTED);
      // light up all reachable states
      for (Position pos : model.getAllMovesFrom(row, col)) {
        TileState state = model.getPieceAt(pos.row, pos.col).isPresent() ? 
          TileState.ATTACKABLE : TileState.REACHABLE;
        this.setBackgroundAt(pos.row, pos.col, state);
      }
      lastSelected = Optional.of(Position.of(row, col));
    });
  }

  /**
   * Blend the 2 given colors, assigning `w1` to the first color.
   * ASSUME firstWeight ∈ [0, 1]
   */
  private static Color blendColors(Color c1, Color c2, float w1) {
    float w2 = 1 - w1;
    return new Color(
        (int)(c1.getRed() * w1   + c2.getRed() * w2),
        (int)(c1.getGreen() * w1 + c2.getGreen() * w2),
        (int)(c1.getBlue() * w1  + c2.getBlue() * w2));
  }

  /*
   * Change the char surrounding background color for Piece at (row, col) on Chess Board
   * based on the given TileState.
   */
  private void setBackgroundAt(int row, int col, TileState state) {
    var button = this.boardButtons[row][col];
    var color = row % 2 == col % 2 ? Color.GRAY : Color.WHITE;
    switch(state) {
      case REACHABLE: {
        color = blendColors(color, Color.GREEN, 0.6f);
        break;
      }
      case ATTACKABLE: {
        color = blendColors(color, Color.RED, 0.6f);
        break;
      }
      case SELECTED: {
        color = blendColors(color, Color.YELLOW, 0.6f);
        break;
      }
      case NORMAL: break;
      default:
        throw new RuntimeException("TileState not recognized\n");
    }
    button.setBackground(color);
  }
}
