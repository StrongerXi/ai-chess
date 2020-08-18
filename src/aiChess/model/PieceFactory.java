package aiChess.model;


import java.util.Collection;
import java.util.Optional;
import java.util.ArrayList;

/**
 * A factory which generates specific Piece implementations.
 */
final class PieceFactory {
  /* prohibits instantiation of this class */
  private PieceFactory() {}

  /* Maximum steps any piece can take. This is to prevent overflow */
  private static final int MAX_STEP = 10000;

  /**
   * Produce an instance of the piece with given configuration.
   */
  public static Piece makePiece(PieceType type, PlayerType player) {
    return makePieceMoved(type, player, false); // default to not moved
  }

  /**
   * For testing purposes
   */
  static Piece makePieceMoved(PieceType type, PlayerType player, boolean hasMoved) {
    switch(type) {
      case KING:   return new King(player, hasMoved);
      case QUEEN:  return new Queen(player, hasMoved);
      case BISHOP: return new Bishop(player, hasMoved);
      case KNIGHT: return new Knight(player, hasMoved);
      case CASTLE: return new Castle(player, hasMoved);
      case PAWN:   return new Pawn(player, hasMoved);
      default: throw new RuntimeException("PieceType not recognized\n");
    }
  }


  /*---------------------------------------------------------------------------
   * The following classes are Piece instances with move logic corresponding to 
   * the specific type of piece they represent.
   *--------------------------------------------------------------------------- */

  private static final class King extends Piece {
    King(PlayerType owner, boolean hasMoved) {
      super(owner, PieceType.KING, hasMoved);
    }
    Piece setMoved(boolean state) {
      return (state == super.hasMoved) ? this : new King(owner, state);
    }
    public Collection<Move> getAllMovesFrom(BoardModel model, int row, int col) {
      Collection<Move> moves = new ArrayList<>();
      var kingPos = new Position(row, col);
      addCrossMoves(model, owner, kingPos, 1, moves);
      addDiagonalMoves(model, owner, kingPos, 1, moves);
      tryAddCastlingMoves(model, row, col, moves);
      return moves;
    }

    /**
     * Try add castling moves to `moves`.
     * ASSUME (row, col) contains a King on `board.`
     */
    private void tryAddCastlingMoves(BoardModel board, int row, int col, Collection<Move> moves) {
      // castling, 3 rules
      // - king and castle not moved
      // - in between pieces empty and not under attack
      // - king not in check (i.e. under attack)
      var king = board.getPieceAt(row, col).get();
      var kingPos = new Position(row, col);
      if (king.hasMoved) {
        return;
      }
      // try both castles
      for (int colDelta = -1; colDelta <= 1; colDelta += 2) {
        var castleCol = col + colDelta;
        if (castleCol < 0 || castleCol >= board.width) {
          continue;
        }
        // find the first non-empty piece, candidate for castling
        while (1 <= castleCol && castleCol < board.width - 1 &&
               board.getPieceAt(row, castleCol).isEmpty()) {
          castleCol += colDelta;
        }
        Optional<Piece> pieceOpt;
        Piece piece;
        if (castleCol != col + colDelta && // needs at least 1 space btw king/castle
            (pieceOpt = board.getPieceAt(row, castleCol)).isPresent() &&
            (piece = pieceOpt.get()).owner == king.owner && piece.type == PieceType.CASTLE && !piece.hasMoved) {
          // both king and target castle haven't moved
          boolean allEmpty = true;
          var safePos = new ArrayList<Position>();
          for (int i = 1; i < Math.abs(castleCol - col); i += 1) {
            pieceOpt = board.getPieceAt(row, col + i * colDelta);
            if (pieceOpt.isPresent()) {
              allEmpty = false;
              break;
            }
            safePos.add(new Position(row, col + i * colDelta));
          }
          // make sure king and those empty positions are not under attack
          safePos.add(kingPos);
          var opponent = (this.owner == PlayerType.TOP_PLAYER) ?
                          PlayerType.BOTTOM_PLAYER : PlayerType.TOP_PLAYER;
          if (allEmpty && anyUnderAttack(safePos, board, opponent)) {
            moves.add(MoveFactory.makeCastling(kingPos, new Position(row, castleCol - colDelta)));
          }
        }
      }
    }

    /**
     * Return whether any of the given positions attacked by `player` in `board`.
     * **NOTE** It won't consider castling mvoes for `player`.
     */
    static private boolean anyUnderAttack(Collection<Position> positions, BoardModel board, PlayerType player) {
      for (int row = 0; row < board.height; row += 1) {
        for (int col = 0; col < board.width; col += 1) {
          Optional<Piece> pieceOpt;
          Piece piece;
          if ((pieceOpt = board.getPieceAt(row, col)).isEmpty() ||
              (piece = pieceOpt.get()).owner != player) {
            continue;
          }
          Collection<Move> moves = new ArrayList<>();
          if (piece.type != PieceType.KING) {
            moves = piece.getAllMovesFrom(board, row, col);
          } else if (piece.type == PieceType.KING) {
            addCrossMoves(board, piece.owner, new Position(row, col), 1, moves);
            addDiagonalMoves(board, piece.owner, new Position(row, col), 1, moves);
          }
          if (moves.stream().anyMatch(m -> positions.contains(m.targetPos))) {
            // some move from (row, col) attacks a position in `positions`
            return false;
          }
        }
      }
      return true;
    }
  }


  private static final class Queen extends Piece {
    Queen(PlayerType owner, boolean hasMoved) {
      super(owner, PieceType.QUEEN, hasMoved);
    }
    Piece setMoved(boolean state) {
      return (state == super.hasMoved) ? this : new Queen(owner, state);
    }
    public Collection<Move> getAllMovesFrom(BoardModel model, int row, int col) {
      /* There is no step size limit for Queen */
      Collection<Move> moves = new ArrayList<>();
      Position origin = new Position(row, col);
      addCrossMoves(model, owner, origin, MAX_STEP, moves);
      addDiagonalMoves(model, owner, origin, MAX_STEP, moves);
      return moves;
    }
  }


  private static final class Bishop extends Piece {
    Bishop(PlayerType owner, boolean hasMoved) {
      super(owner, PieceType.BISHOP, hasMoved);
    }
    Piece setMoved(boolean state) {
      return (state == super.hasMoved) ? this : new Bishop(owner, state);
    }
    public Collection<Move> getAllMovesFrom(BoardModel model, int row, int col) {
      Collection<Move> moves = new ArrayList<>();
      // There is no step size limit for Bishop
      addDiagonalMoves(model, owner, new Position(row, col), MAX_STEP, moves);
      return moves;
    }
  }


  private static final class Castle extends Piece {
    Castle(PlayerType owner, boolean hasMoved) {
      super(owner, PieceType.CASTLE, hasMoved);
    }
    Piece setMoved(boolean state) {
      return (state == super.hasMoved) ? this : new Castle(owner, state);
    }
    public Collection<Move> getAllMovesFrom(BoardModel model, int row, int col) {
      Collection<Move> moves = new ArrayList<>();
      // There is no step size limit for Castle
      addCrossMoves(model, owner, new Position(row, col), MAX_STEP, moves);
      return moves;
    }
  }


  private static final class Pawn extends Piece {
    Pawn(PlayerType owner, boolean hasMoved) {
      super(owner, PieceType.PAWN, hasMoved);
    }
    Piece setMoved(boolean state) {
      return (state == super.hasMoved) ? this : new Pawn(owner, state);
    }
    public Collection<Move> getAllMovesFrom(BoardModel model, int row, int col) {

      Collection<Move> moves = new ArrayList<>();
      Position origin = new Position(row, col);

      // top owner's pawn only moves downward
      int offset = this.owner == PlayerType.TOP_PLAYER ? -1 : 1;
      int nextRow = row + offset;

      // consider the immediate forward spot
      if (0 <= nextRow && nextRow < model.height) {
        if (model.getPieceAt(row + offset, col).isEmpty()) {
          moves.add(this.getPawnMove(model, origin, nextRow, col));

          // consider a 2-step jump
          nextRow += offset;
          if (!super.hasMoved && 
              0 <= nextRow && nextRow < model.height &&
              model.getPieceAt(nextRow, col).isEmpty()) {
              moves.add(this.getPawnMove(model, origin, nextRow, col));
          }
          nextRow -= offset; // make sure `nextRow = row + offset` on exit
        }

        // consider left/right diagonal attacks
        for (int nextCol = col - 1; nextCol <= col + 1; nextCol += 2) {
          if (0 <= nextCol && nextCol < model.height) {
            var target = model.getPieceAt(nextRow, nextCol);
            if (target.isPresent() && target.get().owner != this.owner) {
              moves.add(this.getPawnMove(model, origin, nextRow, nextCol));
            }
          }
        }
      }
      return moves;
    }

    // pawn promotion or regular move, depending on `targetCol`
    private Move getPawnMove(BoardModel board, Position sourcePos, int targetRow, int targetCol) {
      var promotionRow = (this.owner == PlayerType.TOP_PLAYER) ? 0 : board.height - 1;
      var targetPos = new Position(targetRow, targetCol);
      return (targetRow == promotionRow) ?
             MoveFactory.makePawnPromotion(sourcePos, targetPos) :
             MoveFactory.makeRegularMove(sourcePos, targetPos);
    }
  }


  private static final int[][] knightOffsets = new int[][]{{1, -2}, {2, -1}, {2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}};
  private static final class Knight extends Piece {
    Knight(PlayerType owner, boolean hasMoved) {
      super(owner, PieceType.KNIGHT, hasMoved);
    }
    Piece setMoved(boolean state) {
      return (state == super.hasMoved) ? this : new Knight(owner, state);
    }
    Collection<Move> getAllMovesFrom(BoardModel model, int row, int col) {
      Collection<Move> moves = new ArrayList<>();
      addMovesInDirection(model, owner, new Position(row, col), knightOffsets, 1, moves);
      return moves;
    }
  }



  /*---------------------------------------------------------------------------
   * The followings are helper methods which abstract common pattern of moves
   *--------------------------------------------------------------------------- */

  private static int[][] crossOffsets = new int[][]{{0, 1}, {-1, 0}, {0, -1}, {1, 0}};
  /**
   * Allows a Piece to move horizontally or vertically, with a given maximum step; 
   * add all valid moves into given collection.
   * NOTE origin is exlcuded from target consideration
   * @param model is the current state of board
   * @param origin is where the piece is located
   * @param maxStep is the maximum step the piece can move
   * @param player is the owner of the piece
   * @param validMoves will store all the valid moves.
   */
  private static void addCrossMoves(BoardModel model,
                                    PlayerType player,
                                    Position origin,
                                    int maxStep,
                                    Collection<Move> validMoves) {
    addMovesInDirection(model, player, origin, crossOffsets, maxStep, validMoves);
  }

  private static int[][] diagonalOffsets = new int[][]{{1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
  /**
   * Allows a Piece to move dialognally, with a given maximum step; 
   * add all valid moves into given collection.
   * NOTE origin is exlcuded from target consideration
   * @param model is the current state of board
   * @param origin is where the piece is located
   * @param maxStep is the maximum step the piece can move
   * @param player is the owner of the piece
   * @param validMoves will store all the valid moves.
   */
  private static void addDiagonalMoves(BoardModel model,
                                       PlayerType player,
                                       Position origin,
                                       int maxStep,
                                       Collection<Move> validMoves) {
    addMovesInDirection(model, player, origin, diagonalOffsets, maxStep, validMoves);
  }


  /**
   * Allows a Piece to move in certain direction, with a maximum step; 
   * add all valid moves into given collection.
   * NOTE origin is exlcuded from target consideration
   * @param model is the current state of board
   * @param player is the owner of the piece
   * @param origin is where the piece is located
   * @param offsets is an array of [r, c] which represents direction vectors.
   * @param maxStep is the maximum step the piece can move
   * @param validMoves will store all the valid moves.
   */
  private static void addMovesInDirection(BoardModel model,
                                          PlayerType player,
                                          Position origin,
                                          int[][] offsets,
                                          int maxStep,
                                          Collection<Move> validMoves) {

    int oriRow = origin.row, oriCol = origin.col;

    /* use (dr, dc) as a directional vector, go towards this direction
     * until encountering boundary or another piece */
    for (int[] offset : offsets) {
      int dr = offset[0];
      int dc = offset[1];
      /* number of steps taken in (dr, dc) direction */
      int steps = 1;
      for (int row = oriRow + dr, col = oriCol + dc;  /* do not include origin */
           0 <= col && col < model.width &&
           0 <= row && row < model.height &&
           steps <= maxStep;
           row += dr, col += dc, steps += 1) {

        Optional<Piece> target = model.getPieceAt(row, col);
        Move move = MoveFactory.makeRegularMove(origin, new Position(row, col));

        /* stop after encountering another piece */
        if (target.isPresent()) {
          /* attackable if it's enemy piece */
          if (target.get().owner != player) validMoves.add(move);
          break;
        }
        validMoves.add(move);
      }
    }
  }
}
