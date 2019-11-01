package aiChess.model;

import java.util.Optional;

/**
 */
public abstract class Move {

  /* read only attributes */
  public final Position sourcePos;
  public final Position targetPos;
  public final Piece sourcePiece;
  public final Optional<Piece> targetPiece;

  
  /**
   * Constructor.
   */
  public Move(Position sourcePos, Position targetPos, Piece sourcePiece, Optional<Piece> targetPiece) {
    this.sourcePos = sourcePos;
    this.targetPos = targetPos;
    this.sourcePiece = sourcePiece;
    this.targetPiece = targetPiece;
  }


  /**
   * Apply this move to the given model.
   * ASSUME the source and target pieces agree.
   */
  abstract void apply(BoardModel model);


  /**
   * Apply this move in reverse to the given model.
   * ASSUME the source and target pieces agree.
   */
  abstract void undo(BoardModel model);
}
