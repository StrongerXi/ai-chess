package aiChess.model;

import java.util.Optional;


/**
 * A factory which generates concrete moves representing:
 * - regular move
 * - EnPassant (TODO)
 */
final class MoveFactory {

  /* Prevent instantiation */
  private MoveFactory() {}

  public static Move makeRegularMove(Position source, Position target) {
    return new RegularMove(source, target);
  }

  // assume the positions are valid
  public static Move makeCastling(Position kingSrc, Position kingDst) {
    return new Castling(kingSrc, kingDst);
  }

  public static Move makePawnPromotion(Position source, Position target) {
    return new PawnPromotion(source, target);
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

  /**
   * A castling move.
   */
  private static class Castling extends Move {
    /**
     * Constructor.
     * source and target positions in Move are interpreted for King here.
     */
    public Castling(Position kingSrc, Position kingDst) {
      super(kingSrc, kingDst);
      assert(kingSrc.row == kingDst.row);
    }

    @Override
    void apply(BoardModel model) {
      // assume castle is in right position and state
      int castleSrcCol = targetPos.col + Integer.signum(targetPos.col - sourcePos.col);
      int castleDstCol = targetPos.col - Integer.signum(targetPos.col - sourcePos.col);
      var king   = model.getPieceAt(sourcePos.row, sourcePos.col);
      var castle = model.getPieceAt(targetPos.row, castleSrcCol);
      assert(king.get().type == PieceType.KING);
      assert(!king.get().hasMoved);
      assert(!castle.get().hasMoved);
      assert(castle.get().type == PieceType.CASTLE);
      // move to new position, clear old positions
      model.setPieceAt(targetPos.row, targetPos.col, king.map(p -> p.setMoved(true)));
      model.setPieceAt(sourcePos.row, sourcePos.col, Optional.empty());
      model.setPieceAt(targetPos.row, castleDstCol, castle.map(p -> p.setMoved(true)));
      model.setPieceAt(targetPos.row, castleSrcCol, Optional.empty());
    }

    @Override
    void undo(BoardModel model) {
      int castleSrcCol = targetPos.col + Integer.signum(targetPos.col - sourcePos.col);
      int castleDstCol = targetPos.col - Integer.signum(targetPos.col - sourcePos.col);
      var king   = model.getPieceAt(targetPos.row, targetPos.col);
      var castle = model.getPieceAt(targetPos.row, castleDstCol);
      // restore then clear
      model.setPieceAt(targetPos.row, castleSrcCol, castle.map(p -> p.setMoved(false)));
      model.setPieceAt(targetPos.row, castleDstCol, Optional.empty());
      model.setPieceAt(sourcePos.row, sourcePos.col, king.map(p -> p.setMoved(false)));
      model.setPieceAt(targetPos.row, targetPos.col, Optional.empty());
    }


    /* NOTE move equality only concerns source and target position
     * not internal state of move. */

    @Override
    public boolean equals(Object o) {
      if (o == null || !(o instanceof Castling)) return false;
      if (o == this) return true;
      Castling other = (Castling) o;
      return this.sourcePos.equals(other.sourcePos)
          && this.targetPos.equals(other.targetPos);
    }

    @Override
    public int hashCode() {
      return (31 * this.sourcePos.hashCode()) * 31 + this.targetPos.hashCode();
    }
  }

  /**
   * A pawn promotion move; promoting to queen by default.
   */
  private static class PawnPromotion extends Move {
    // contains the pawn piece after `apply`
    private Optional<Piece> pawnPiece = Optional.empty();
    private Optional<Piece> targetPiece = Optional.empty();

    /**
     * Constructor.
     */
    public PawnPromotion(Position sourcePos, Position targetPos) {
      super(sourcePos, targetPos);
    }

    @Override
    void apply(BoardModel model) {
      // if sourcePos is unoccupied, both (source/target)Pos become empty
      this.pawnPiece = model.getPieceAt(sourcePos.row, sourcePos.col);
      this.targetPiece = model.getPieceAt(targetPos.row, targetPos.col);
      var queen = pawnPiece.map(p -> PieceFactory.makePiece(PieceType.QUEEN, p.owner));
      model.setPieceAt(sourcePos.row, sourcePos.col, Optional.empty());
      model.setPieceAt(targetPos.row, targetPos.col, queen);
    }

    @Override
    void undo(BoardModel model) {
      // target was assumed to be empty
      model.setPieceAt(targetPos.row, targetPos.col, this.targetPiece);
      model.setPieceAt(sourcePos.row, sourcePos.col, this.pawnPiece);
      this.pawnPiece = Optional.empty();
      this.targetPiece = Optional.empty();
    }

    /* NOTE move equality only concerns source and target position
     * not internal state of move. */

    @Override
    public boolean equals(Object o) {
      if (o == null || !(o instanceof PawnPromotion)) return false;
      if (o == this) return true;
      PawnPromotion other = (PawnPromotion) o;
      return this.sourcePos.equals(other.sourcePos)
          && this.targetPos.equals(other.targetPos);
    }

    @Override
    public int hashCode() {
      return (31 * this.sourcePos.hashCode()) * 31 + this.targetPos.hashCode();
    }
  }
}
