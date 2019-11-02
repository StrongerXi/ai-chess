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
    switch(type) {
      case KING: return makeKing(player);
      case QUEEN: return makeQueen(player);
      case BISHOP: return makeBishop(player);
      case KNIGHT: return makeKnight(player);
      case CASTLE: return makeCastle(player);
      case PAWN: return makePawn(player);
      default: throw new RuntimeException("PieceType not recognized\n");
    }
  }


  /* The following methods return anonymous Piece instances with 
   * move logic corresponding to the specific type of piece they represent. */
  private static Piece makeKing(PlayerType player) {
    return new Piece(player, PieceType.KING) {
      public Collection<Move> getAllMovesFrom(BoardModel model, int row, int col) {
        Collection<Move> moves = new ArrayList<>();
        addCrossMoves(model, player, new Position(row, col), 1, moves);
        addDiagonalMoves(model, player, new Position(row, col), 1, moves);
        return moves;
      }
    };
  }


  private static Piece makeQueen(PlayerType player) {
    return new Piece(player, PieceType.QUEEN) {
      public Collection<Move> getAllMovesFrom(BoardModel model, int row, int col) {
        /* There is no step size limit for Queen */
        Collection<Move> moves = new ArrayList<>();
        Position origin = new Position(row, col);
        addCrossMoves(model, player, origin, MAX_STEP, moves);
        addDiagonalMoves(model, player, origin, MAX_STEP, moves);
        return moves;
      }
    };
  }


  private static Piece makeBishop(PlayerType player) {
    return new Piece(player, PieceType.BISHOP) {
      public Collection<Move> getAllMovesFrom(BoardModel model, int row, int col) {
        Collection<Move> moves = new ArrayList<>();
        /* There is no step size limit for Bishop */
        addDiagonalMoves(model, player, new Position(row, col), MAX_STEP, moves);
        return moves;
      }
    };
  }


  private static Piece makeCastle(PlayerType player) {
    return new Piece(player, PieceType.CASTLE) {
      public Collection<Move> getAllMovesFrom(BoardModel model, int row, int col) {
        Collection<Move> moves = new ArrayList<>();
        /* There is no step size limit for Castle */
        addCrossMoves(model, player, new Position(row, col), MAX_STEP, moves);
        return moves;
      }
    };
  }


  private static Piece makePawn(PlayerType player) {
    return new Piece(player, PieceType.PAWN) {

      public Collection<Move> getAllMovesFrom(BoardModel model, int row, int col) {

        Collection<Move> moves = new ArrayList<>();
        Position origin = new Position(row, col);

        /* top player's pawn only moves downward */
        int offset = this.owner == PlayerType.TOP_PLAYER ? -1 : 1;
        int nextRow = row + offset;

        /* consider the immediate forward spot */
        if (0 <= nextRow && nextRow < model.height) {
          Optional<Piece> target = model.getPieceAt(row + offset, col);
          if (target.isEmpty()) {
            moves.add(MoveFactory.makeRegularMove(origin, new Position(nextRow, col)));

            /* If pawn has not moved, and its forward position is empty
             * also consider a 2-step jump */
            if (!super.hasMoved()) {
              nextRow += offset;
              if (0 <= nextRow && nextRow < model.height) {
                target = model.getPieceAt(nextRow, col);
                if (target.isEmpty()) {
                  moves.add(MoveFactory.makeRegularMove(origin, new Position(nextRow, col)));
                }
              }
            }
          }

          /* also consider diagonal attacks */

          /* left */
          if (col-1 >= 0) {
            target = model.getPieceAt(row + offset, col - 1);
            if (target.isPresent() && target.get().owner != this.owner) {
              moves.add(MoveFactory.makeRegularMove(origin, new Position(row+offset, col-1)));
            }
          }

          /* right */
          if (col+1 < model.width) {
            target = model.getPieceAt(row + offset, col + 1);
            if (target.isPresent() && target.get().owner != this.owner) {
              moves.add(MoveFactory.makeRegularMove(origin, new Position(row+offset, col+1)));
            }
          }
        }
        return moves;
      }

    };
  }



  private static final int[][] knightOffsets = new int[][]{{1, -2}, {2, -1}, {2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}};
  private static Piece makeKnight(PlayerType player) {
    return new Piece(player, PieceType.KNIGHT) {
      public Collection<Move> getAllMovesFrom(BoardModel model, int row, int col) {
        Collection<Move> moves = new ArrayList<>();
        addMovesInDirection(model, player, new Position(row, col), knightOffsets, 1, moves);
        return moves;
      }
    };
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
           0 <= col && col < model.width && 0 <= row && row < model.height
           && steps <= maxStep;
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


