
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
1. Write tests for model package
  - tests for move generation
  - tests for Move logic (apply/undo)
2. Implement classes in model package
  - implement move generation
  - implement move logic
  
3. Implement basic Controller and CLI View
4. Implement GUI View
5. Implement special moves: En Passant, Castling.
