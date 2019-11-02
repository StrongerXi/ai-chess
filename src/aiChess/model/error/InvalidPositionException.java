package aiChess.model.error;
import aiChess.model.Position;

@SuppressWarnings("serial")
public class InvalidPositionException extends RuntimeException {

  public InvalidPositionException(Position pos) {
    super(String.format("%s out of bound\n", pos));
  }

  public InvalidPositionException(int row, int col) {
    this(new Position(row, col));
  }
}
