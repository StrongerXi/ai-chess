package aiChess.model;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

/**
 * Caches positions encountered during search algorithm, and their relevant scores.
 * Assume the scores are evaluation of board for a specific.
 */
class TranspositionTable {

  private static final class Key {
    public final BoardModel board;
    public final PlayerType player;
    Key(BoardModel board, PlayerType player) {
      this.board  = board;
      this.player = player;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || ! (o instanceof Key)) return false;
      var other = (Key) o;
      return this.board.equals(other.board) &&
             this.player == other.player;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.board, this.player);
    }
  }

  // alpha-beta pruning might not yield exact score
  static enum EntryType {
    UPPER, LOWER, EXACT
  }

  static final class Entry {
    public final int score;
    public final EntryType type;
    public final int depth;
    // can't be instantiated outside this file
    private Entry(int score, int depth, EntryType type) {
      this.score = score;
      this.depth = depth;
      this.type  = type;
    }
  }

  private final Map<Key, Entry> cache = new HashMap<>();

  // `board` must be immutable, (use getCopy method if necessary)
  void put(BoardModel board, PlayerType player, int score, int depth, EntryType type) {
    var key = new Key(board, player);
    var val = new Entry(score, depth, type);
    var dup = this.cache.getOrDefault(key, null);
    // deeper means more accurate in general
    if (dup == null || depth > dup.depth) {
      this.cache.put(key, val);
    }
  }

  Optional<Entry> get(BoardModel board, PlayerType player) {
    var key = new Key(board, player);
    var val = this.cache.getOrDefault(key, null);
    return Optional.ofNullable(val);
  }

  int size() {
    return this.cache.size();
  }

  void clear() {
    this.cache.clear();
  }
}
