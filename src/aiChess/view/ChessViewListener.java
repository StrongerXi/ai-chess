package aiChess.view;

/**
 * A callback interface used to handle user input received within the ChessView.
 */
public interface ChessViewListener {
  
  /**
   * Make appropriate response when the user has requested to make a move
   * from (srow, scol) to (drow, dcol).
   * @param srow the row index of source tile
   * @param scol the col index of source tile
   * @param trow the row index of target tile
   * @param tcol the col index of target tile
   */
  void moveRequested(int srow, int scol, int trow, int tcol);


  /**
   * Make appropriate response to the user's undo request.
   */
  void undoRequested();
}
