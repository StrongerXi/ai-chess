package aiChess.model;

import java.util.Objects;
import java.util.Optional;

/**
 * A Move contains all the logic of the concrete move it represents.
 * It allows client to delay the application and reversal of a move to a board model.
 *
 * Equivalence of move is only dependent upon its configuration, not internal state of move.
 */
public abstract class Move {

  // Used to identify the type of a Move
  public enum MoveType {
    REGULAR, CASTLING, PAWN_PROMOTION
  }

  /* read only attributes */
  public final Position sourcePos;
  public final Position targetPos;
  public final MoveType type;

  
  /**
   * Constructor.
   */
  public Move(Position sourcePos, Position targetPos, MoveType type) {
    this.sourcePos = sourcePos;
    this.targetPos = targetPos;
    this.type = type;
  }


  /**
   * Apply this move to the given model.
   * ASSUME the source and target pieces are present and valid.
   */
  abstract void apply(BoardModel model);


  /**
   * Undo the effect of last apply to the given model.
   * ASSUME the source and target pieces are present and valid.
   */
  abstract void undo(BoardModel model);


  @Override
  public String toString() {
    return String.format("[%s] %s to %s", type.toString(), sourcePos, targetPos);
  }

  /* NOTE move equality only concerns source and target position
   * not internal state of move. */

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof Move)) return false;
    if (o == this) return true;
    var other = (Move) o;
    return this.sourcePos.equals(other.sourcePos) &&
           this.targetPos.equals(other.targetPos) &&
           this.type == other.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.sourcePos, this.targetPos);
  }
}
