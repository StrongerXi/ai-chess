package aiChess.model.error;
import aiChess.model.Position;

@SuppressWarnings("serial")
public class InvalidPositionException extends RuntimeException {

  public InvalidPositionException(Position pos) {
    super(String.format("%s out of bound", pos));
  }

  public InvalidPositionException(int row, int col) {
    this(Position.of(row, col));
  }
}
