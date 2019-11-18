
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

To run the application, type:
```
mvn exec:java
```

# TODO
---
2. Add tests first to make sure board generates 'correct moves'
3. What to Add in terms of API
  - Move generate move method into Board class, since AI will need it
  - Add getAllThreatenedLocation(PlayerType player) method to Board class

4. Refine move generation
  - No move into check; pinned pieces
    - Default into board move generation
  - Report checkmate, record state in GameModel.
    - Check at board construction.
    - Check at end of each move.

4. Implement GUI View
5. Implement special moves: En Passant, Castling, Pawn Promotion


##Checkmate Algorithm Idea##
---
__Checkmate__ means 
   - Your king is in check (can be attacked by enemy piece for any pseudo-legal moves).
   - If yes, can you get out of the check

__InCheck Algorithm__
0. ASSUME nobody is in checkmate yet.
1. Calculate attackers (all enemy pieces whose move can directly attack King)
   - Save threatened locations as well?
2. Return these attackers

__LegalMoveAlgorithm__
1. __Pinned pieces__ moves (Skip if being double checked?)
   - Moves from the opponent’s sliding pieces (candidate pinning pieces)
   - “Sliding piece” moves from my king in the opposite direction.
   - The overlap of these two rays.
   - If the result of (3) lands on one of my pieces, then it is pinned. (pinning piece identified as
     well)
   - Once all the pinned pieces are identified, remove them from the board and calculate the moves 
     from the enemy’s pinning piece to your king. This will give you a “ray” of legal moves for each of your pinned pieces.
   - Pinned piece legal moves = pseudo-legal-moves Union the-ray.
2. __King__ can't move into threatened locations (target of all pseudo-legal moves by enemy)
   - Remove King from the board, calculate threatened locations (king can't move in the direction of
     enemy slider's attack ray!)
3. __Any other piece__, 
   - If not inCheck: simply generate pseudo-legal moves.
   - If inCheck: Can we block/capture the "checking enemies"?
    - generate all moves, try make each, and see if it will block _all_ "checking enemies"
     - Could collect paths from "checking enemies" to king. Union of them contains "savior" moves.
     - Store positions of the "checking enemies" themselves in the path too, making a move to that
        location represents "capturing the checking enemy"

## Thoughts ##
---
1. For checkmate detection. Can we simply check if there is any legal move? 
   In other words, can we assume no-legal-move iff is-checkmate?


## Resources ##
---
__General Ideas__:
- [efficient legal move generation](https://peterellisjones.com/posts/generating-legal-chess-moves-efficiently/)
- [Move Generation Validation with Perft Score](http://mediocrechess.blogspot.com/2007/01/guide-perft-scores.html)
- [Search Algorithms](http://www.frayn.net/beowulf/theory.html)

__Code references__:
- [python-chess](https://github.com/niklasf/python-chess/blob/035e32b061430b36752bd994f36a86e4df25886d/chess/__init__.py)
- [java-chess](https://github.com/Vadman97/ChessGame/blob/master/src/vad/GameBoard.java)
