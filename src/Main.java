
import aiChess.view.ChessView;
import aiChess.view.TextualView;
import aiChess.view.SwingView;
import aiChess.control.ChessController;
import aiChess.model.ChessGameModel;

import java.io.InputStreamReader;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * Entry class of the application.
 */
public class Main {

  // Command line arguments for the application
  private static class Args {
    @Parameter(names = "--view", description = "The type of view to be used. { text, gui }")
    public String view = "gui";
    @Parameter(names = "--help", description = "Print out user instructions")
    public boolean help = false;
  }

  public static void main(String[] argv) {
    var args = new Args();
    var parser = JCommander.newBuilder().addObject(args).build();
    parser.parse(argv);
    if (args.help) {
      parser.usage();
      return;
    }

    ChessGameModel model = new ChessGameModel();
    ChessView view;
    switch (args.view) {
      case "text": {
        Readable input = new InputStreamReader(System.in);
        Appendable output = System.out;
        view = new TextualView(input, output);
        break;
      }
      case "gui" : {
        view = new SwingView(model);
        break;
      }
      default:
        throw new IllegalArgumentException("Unsupported view: " + args.view);
    }
    ChessController control = new ChessController(model, view);
  }
}
