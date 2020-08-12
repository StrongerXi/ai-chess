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

    // the source/target piece from last apply, used to undo move.
    private Optional<Piece> targetPiece = Optional.empty();
    private Optional<Piece> sourcePiece = Optional.empty();

    /**
     * Constructor.
     */
    public RegularMove(Position sourcePos, Position targetPos) {
      super(sourcePos, targetPos);
    }

    @Override
    void apply(BoardModel model) {
      // save source and target piece and mutate model
      this.sourcePiece = model.getPieceAt(sourcePos.row, sourcePos.col);
      this.targetPiece = model.getPieceAt(targetPos.row, targetPos.col);
      model.setPieceAt(sourcePos.row, sourcePos.col, Optional.empty());
      model.setPieceAt(targetPos.row, targetPos.col, sourcePiece.map(p -> p.setMoved(true)));
    }

    @Override
    void undo(BoardModel model) {
      // restore source and target piece in model
      model.setPieceAt(targetPos.row, targetPos.col, this.targetPiece);
      model.setPieceAt(sourcePos.row, sourcePos.col, this.sourcePiece);
      // clear saved info from last apply
      sourcePiece = Optional.empty();
      targetPiece = Optional.empty();
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
