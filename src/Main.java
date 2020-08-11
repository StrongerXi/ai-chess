
import aiChess.view.ChessView;
import aiChess.view.TextualView;
import aiChess.view.SwingView;
import aiChess.control.ChessController;
import aiChess.model.ChessGameModel;

import java.io.InputStreamReader;

import javax.swing.JFrame;


/**
 * Entry class of the application.
 */
public class Main {

  public static void main(String[] args) {
    Readable input = new InputStreamReader(System.in);
    Appendable output = System.out;

    ChessGameModel model = new ChessGameModel();
    //ChessView view = new TextualView(input, output);
    ChessView view = new SwingView(model);
    ChessController control = new ChessController(model, view);
  }
}
