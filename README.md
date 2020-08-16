
# Chess Game
---
This is a standard chess game implemented in Java.
It allows 2 players, each of which can be either human or AI.

# Build
---
To build the project, go to root of the project and type the following via CLI
```
mvn compile
```

To run the application and see user instructions:
```
./run --help
```

# TODO
---
- Make Piece immutable
- Add AI component
- clean up documentation and codebase
  - RegularMove class
  - remove redundant hashcode/equal methods
  - accept Position, for ease of testing?
- Implement and benchmark FastLegalMove algorithm
- Implement special moves: En Passant, Castling, Pawn Promotion


##Checkmate Algorithm Idea##
---
__In check__  := king is reachable by _any_ enemy piece pseudo-legally
__Stalemate__ := can't move, king not in check
__Checkmate__ := can't move, king in check

__LegalMove Algorithm__
1. Try each pseudo-legal moves
2. Abandon the move if King can be reached by any enemy piece

__FastLegalMove Algorithm__
1. King moves (detect in-check).
 (a) Get its pseudo-legal moves
 (b) Remove it, find threatened locations, subtract from (a)
     - Anywhere an enemy piece can reach (pseudo-legally)
     - Any enemy piece reachable by _another_ enemy piece
     Conveniently, also determine whether king is in check
 (c) If checked by more than 1 piece, only King moves matter. 
     (if we don't introduce crazy moves)

2. Identify pinned pieces
 (a) Find pieces threatened by enemy sliders (TODO isSlider method)
 (b) Put slider on King's position, can it still reach corresponding piece?
 (c) If yes, it's pinned by that slider.

3. If not in check:
 (a) Pinned piece moves: to/from the pinning piece.
     - remove pinned piece from board
     - calculate path from pinning piece to King (TODO pathTo method)
 (b) others: generate pseudo-legal moves.

4. If in check
 (a) Pinned piece can't move
 (b) others: must capture or block the checking enemy, based on its type.
     - slider, can be captured or blocked.
     - non-slider, must be captured


## Resources ##
---
__General Ideas__:
- [efficient legal move generation](https://peterellisjones.com/posts/generating-legal-chess-moves-efficiently/)
- [Move Generation Validation with Perft Score](http://mediocrechess.blogspot.com/2007/01/guide-perft-scores.html)
- [Search Algorithms](http://www.frayn.net/beowulf/theory.html)
- [Search Algorithm
  illustrated](https://www.freecodecamp.org/news/simple-chess-ai-step-by-step-1d55a9266977/)
- [alphabeta and transposition](https://en.wikipedia.org/wiki/Negamax#cite_note-Breuker-1)
- [mtdf algorithm](http://people.csail.mit.edu/plaat/mtdf.html#abmem)

__Code references__:
- [python-chess](https://github.com/niklasf/python-chess/blob/035e32b061430b36752bd994f36a86e4df25886d/chess/__init__.py)
- [java-chess](https://github.com/Vadman97/ChessGame/blob/master/src/vad/GameBoard.java)
