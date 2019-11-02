package aiChess.model.error;
import aiChess.model.Move;

@SuppressWarnings("serial")
public class InvalidMoveException extends RuntimeException {

  /**
   * Exception for an invalid move from (srow, scol) to (drow, dcol).
   */
  public InvalidMoveException(int srow, int scol, int drow, int dcol) {
    super(String.format("move: (%d, %d) to (%d, %d) is not valid\n", srow, scol, drow, dcol));
  }
}
