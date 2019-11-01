package aiChess.model;

import java.util.Optional;

/**
 * A Move contains all the logic of the concrete move it represents.
 * It allows client to delay the application and reversal of a move to a board model.
 *
 * Equivalence of move is only dependent upon its configuration, not internal state of move.
 */
public abstract class Move {

  /* read only attributes */
  public final Position sourcePos;
  public final Position targetPos;

  /* used to undo move */
  private Optional<Piece> targetPiece = Optional.empty();
  /* True iff the apply method was invoked last */
  private boolean lastApplied = false;

  
  /**
   * Constructor.
   */
  public Move(Position sourcePos, Position targetPos) {
    this.sourcePos = sourcePos;
    this.targetPos = targetPos;
  }


  /**
   * Apply this move to the given model.
   * ASSUME the source and target pieces agree.
   */
  abstract void apply(BoardModel model);


  /**
   * Undo the effect of last apply to the given model.
   * ASSUME the source and target pieces agree.
   */
  abstract void undo(BoardModel model);
}
