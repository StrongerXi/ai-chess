package aiChess.model;


/**
 * A factory which generates specific Piece implementations.
 */
final class PieceFactory {
  /* prohibits instantiation of this class */
  private PieceFactory() {}

  /**
   * Produce an instance of the piece with given configuration.
   */
  public static Piece makePiece(PieceType type, PlayerType player) {
    /*
    switch(type) {
      case KING: return makeKing(row, col, player);
      case QUEEN: return makeQueen(row, col, player);
      case BISHOP: return makeBishop(row, col, player);
      case KNIGHT: return makeKnight(row, col, player);
      case CASTLE: return makeCastle(row, col, player);
      case PAWN: return makePawn(row, col, player);
      default: throw new RuntimeException("PieceType not recognized\n");
    }
    */
  /* TODO */
    return null;
  }


  /* TODO */
}


