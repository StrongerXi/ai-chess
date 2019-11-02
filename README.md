
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
1. Implement additional Game feature
  - checkmate detection (that is, the King cannot escape)
    - Improve King's move generation to detect threatened tiles?
  - draw detection?
4. Implement GUI View
5. Implement special moves: En Passant, Castling, Pawn Promotion
