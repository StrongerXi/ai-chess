package aiChess.model;

/**
 * Types of Piece allowed in the chess game.
 */
public enum PieceType {
  KING, QUEEN, BISHOP, KNIGHT, CASTLE, PAWN;

  public String toString() {
    switch (this) {
      case KING : return "king";
      case QUEEN : return "queen";
      case BISHOP : return "bishop";
      case CASTLE : return "castle";
      case PAWN : return "pawn";
      default: return "knight";
    }
  }
}
