package aiChess.model;

import java.util.Optional;


/**
 * A factory which generates concrete moves representing:
 * - regular move
 * - EnPassant (TODO)
 * - Castling (TODO)
 * - Pawn promotion (TODO)
 */
final class MoveFactory {

  /* Prevent instantiation */
  private MoveFactory() {}

  public static Move makeRegularMove(Position source, Position target) {
    return new RegularMove(source, target);
  }


  /**
   * A regular move which applies to all types of Piece.
   */
  private static class RegularMove extends Move {

    /* True iff the apply method was invoked last */
    private boolean lastApplied = false;
    /* the target piece from last apply, used to undo move */
    private Optional<Piece> targetPiece = Optional.empty();
    /* whether the source piece has moved before the last apply */
    private boolean moveStatus = false;


    /**
     * Constructor.
     */
    public RegularMove(Position sourcePos, Position targetPos) {
      super(sourcePos, targetPos);
    }

    @Override
    void apply(BoardModel model) {
      Optional<Piece> sourcePiece = model.getPieceAt(sourcePos.row, sourcePos.col);

      /* save target piece for future undo */
      targetPiece = model.getPieceAt(targetPos.row, targetPos.col);
      this.lastApplied = true;

      /* NOTE assume the source piece can't be empty */
      this.moveStatus = sourcePiece.get().hasMoved();
      sourcePiece.get().setMoved(true);

      /* make source location empty */
      model.setPieceAt(sourcePos.row, sourcePos.col, Optional.empty());
      /* move source piece to target location */
      model.setPieceAt(targetPos.row, targetPos.col, sourcePiece);
    }

    @Override
    void undo(BoardModel model) {
      Optional<Piece> sourcePiece = model.getPieceAt(targetPos.row, targetPos.col);
      /* move saved target piece to target location */
      model.setPieceAt(targetPos.row, targetPos.col, this.targetPiece);
      /* move source piece to source location */
      model.setPieceAt(sourcePos.row, sourcePos.col, sourcePiece);
      /* clear saved info from last apply */
      targetPiece = Optional.empty();
      this.lastApplied = false;
      sourcePiece.get().setMoved(this.moveStatus);
    }


    /* NOTE move equality only concerns source and target position 
     * not internal state of move. */

    @Override
    public boolean equals(Object o) {
      if (o == null || !(o instanceof RegularMove)) return false;
      if (o == this) return true;
      RegularMove other = (RegularMove) o;
      return this.sourcePos.equals(other.sourcePos)
          && this.targetPos.equals(other.targetPos);
    }

    @Override
    public int hashCode() {
      return (31 * this.sourcePos.hashCode()) * 31 + this.targetPos.hashCode();
    }
  }
}
