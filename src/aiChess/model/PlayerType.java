package aiChess.model;

/**
 * Represents the type of player, based on where their pieces started in a standard chess game.
 * NOTE
 * There can be certain starting configuration which breaks the notion of TOP/BOTTOM,
 * but the orientation of move stays the same (TOP_PLAYER's pawn can only move downward).
 */
public enum PlayerType {
  TOP_PLAYER, BOTTOM_PLAYER;

  public String toString() {
    switch (this) {
      case TOP_PLAYER : return "top";
      default :         return "bottom";
    }
  }

  public PlayerType getOpponent() {
    switch (this) {
      case TOP_PLAYER : return BOTTOM_PLAYER;
      default :         return TOP_PLAYER;
    }
  }
}

