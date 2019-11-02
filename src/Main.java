
import aiChess.view.ChessView;
import aiChess.view.TextualView;
import aiChess.control.ChessController;
import aiChess.model.ChessGameModel;

import java.io.InputStreamReader;



/**
 * Entry class of the application.
 */
public class Main {

  public static void main(String[] args) {
    Readable input = new InputStreamReader(System.in);
    Appendable output = System.out;

    ChessGameModel model = new ChessGameModel();
    ChessView view = new TextualView(input, output);
    ChessController control = new ChessController(model, view);
  }
}
