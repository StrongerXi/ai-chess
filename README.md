# Chess Game
This is a standard chess game implemented in Java.
It supports 2 players, each of which can be either human or AI.

# Build
To build the project, go to root of the project and type the following via CLI
```
mvn compile
```

To run the application and see user instructions:
```
./run --help
```

## Resources
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
