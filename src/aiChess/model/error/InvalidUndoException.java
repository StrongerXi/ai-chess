package aiChess.model.error;
import aiChess.model.Position;

@SuppressWarnings("serial")
public class InvalidUndoException extends RuntimeException {

  public InvalidUndoException(String msg) {
    super(msg);
  }
}
