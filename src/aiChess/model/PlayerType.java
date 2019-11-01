package aiChess.model;

/**
 * Represents the type of player, based on where their pieces started in a standard chess game.
 * NOTE
 * There can be certain starting configuration which breaks the notion of TOP/BOTTOM,
 * but the orientation of move stays the same (TOP_PLAYER's pawn can only move downward).
 */
public enum PlayerType {
  TOP_PLAYER, BOTTOM_PLAYER;
}

