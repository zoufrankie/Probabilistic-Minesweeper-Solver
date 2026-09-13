# Minesweeper with Automated Solver

A Java implementation of Minesweeper with a Swing GUI and an automated solver
that plays the game using three escalating strategies: basic rule-based
deduction, pairwise set theory, and exact probabilistic reasoning by
constraint enumeration.

---

## Overview

The project has two parts:

1. **The game** (`Game.java`, `Grid.java`) — a playable 10×10 Minesweeper board
   with 20 mines, a timer, flagging, flood-fill reveal, and win/loss detection.
2. **The solver** (`Computer.java`) — an automated player that reads the board,
   deduces mine locations, and reveals or flags tiles. When no guaranteed move
   exists, it computes exact per-tile mine probabilities and plays the safest
   tile.

The solver runs in a loop (`App.java`), escalating from cheap rules to expensive
reasoning only when necessary.

---

## Features

### Game
- 10×10 grid with 20 mines (configurable via `rows`, `cols`, `totalMines`)
- Mines placed after the first click, guaranteeing a safe opening area
- Left-click to reveal, right-click (or Ctrl+left-click) to flag
- Flood-fill reveal for tiles with no adjacent mines
- Per-number color coding (1–8)
- Live timer, flags-remaining display, restart button
- Win/loss detection with mine reveal on game over

### Solver
- **Stage 1 — Default deduction:** guaranteed mine flags and guaranteed safe reveals
- **Stage 2 — Set theory:** pairwise subset deduction that resolves 1-2-1 and
  1-2-2-1 patterns the basic rules miss
- **Stage 3 — Probabilistic:** exact per-tile mine probability via frontier
  constraint enumeration, with connected-component splitting and pruning
- Falls back to a local heuristic on components too large to brute-force
- Plays the lowest-probability tile when no guaranteed move exists

---

## Project Structure

```
├── App.java          ← Entry point; runs the solver loop
├── Game.java         ← Game logic, GUI, board setup, mine placement
├── Computer.java     ← Automated solver (extends Game)
└── Grid.java         ← Tile class (extends JButton); state + neighbor links
```

---

## How the Solver Works

The solver runs three stages in order, only advancing when the previous stage
stops making progress.

### Stage 1 — Default deduction

Two rules, applied every cycle.

**`findBombs()` — flag guaranteed mines**

For every revealed tile, count its unrevealed neighbors. If that count equals
the tile's number, every one of those neighbors is a mine.

```
if (tile.number == unrevealedNeighbors) → flag all unrevealed neighbors
```

**`clearTiles()` — reveal guaranteed safe tiles**

For every revealed tile, count its flagged neighbors. If that count equals the
tile's number, all remaining unrevealed neighbors are safe.

```
if (tile.number == flaggedNeighbors) → reveal all other unrevealed neighbors
```

Revealing a tile with zero adjacent mines triggers `removeTile()`, which
recursively flood-fills outward.

### Stage 2 — Set theory

Pairwise subset deduction (`setTheories()` / `setTheory()`). For each revealed
numbered tile, compare it against each neighboring numbered tile:

1. Build each tile's set of hidden, unflagged neighbors.
2. Subtract the two sets to find each tile's **exclusive** group.
3. If `minesA > minesB` and `(minesA - minesB) == size(A exclusive)`, then A's
   exclusive tiles are all mines and B's exclusive tiles are all safe (and vice
   versa).

This resolves patterns the basic rules cannot, e.g.:

```
[1][2][1]
 A  B  C
```

The `1`s and `2` together force B to be a mine and A, C to be safe.

### Stage 3 — Probabilistic reasoning

When stages 1 and 2 stall, `guess()` runs.

**`edgeTiles()`** returns every hidden, unflagged tile adjacent to a revealed
tile. These are the only tiles whose mine status can be inferred from the
visible board.

**`probability(frontier)`** computes an exact per-tile mine probability:

1. Index the frontier tiles.
2. Build one constraint per revealed numbered tile:
   `sum(mines in hidden neighbors) == number − flaggedNeighbors`.
3. Union-find the frontier into independent components (tiles sharing a constraint).
4. Backtrack each component, pruning partial assignments that already violate a
   constraint.
5. Tally, per tile, how often it is a mine across all valid configurations.

Result is stored on each `Grid` tile as an integer 0–100.

**`guess()`** then makes the safest possible move:

- Reveal any tile at **0%** (guaranteed safe)
- Flag any tile at **100%** (guaranteed mine)
- Otherwise reveal the **lowest-probability** tile

Components larger than 22 tiles skip enumeration and fall back to a per-tile
heuristic (average of adjacent local mine ratios), to keep runtime bounded.

---

## How to Run

**Requirements:** Java 8 or later (uses `java.awt` and `javax.swing`).

```bash
javac *.java
java App
```

`App` launches the solver against the board automatically. To play manually
instead, uncomment `Game game = new Game();` in `App.java` and comment out the
solver block.

The solver's first move is made by `Computer.startGame()`, which places mines,
starts the timer, and reveals the center tile. Without this call, no tiles would
be revealed and the solver would have nothing to reason about.

---

## Configuration

Edit these constants in `Game.java`:

| Constant     | Default | Meaning             |
|--------------|---------|---------------------|
| `rows`       | 10      | Board height        |
| `cols`       | 10      | Board width         |
| `totalMines` | 20      | Number of mines     |
| `tileSize`   | 30      | Tile size in pixels |

To match classic difficulty presets:
- **Beginner:** 9×9, 10 mines
- **Intermediate:** 16×16, 40 mines
- **Expert:** 24×20, 99 mines

To change solver pacing, adjust the `Thread.sleep(...)` in `App.java` and the
`ticker % 4` interval that controls how often set theory runs.

---

## Design Notes

- **Neighbor links over index math.** Each `Grid` tile holds direct references
  to its eight neighbors, set once during board construction. The solver
  traverses the board without repeated bounds checking.
- **Escalating cost.** Stage 1 is O(rows × cols) and catches most moves. Stage 2
  is O(rows × cols × neighbors) and catches patterns Stage 1 misses. Stage 3 is
  exponential in frontier size but only runs when nothing else works.
- **Logic before guessing.** Every Stage 1 and Stage 2 move is provably correct.
  The solver never guesses while a guaranteed move exists.
- **Component splitting.** Frontier constraints are usually disconnected, so
  union-find turns one `2^n` enumeration into several small ones. A 20-tile
  frontier in one component is 1M configurations; split into four 5-tile
  components it's 128.
- **Pruning.** `partialOK()` rejects partial assignments as soon as a constraint
  is violated in either direction — too many mines already, or too few remaining
  slots to reach the required count.

---

## Future Enhancements

- Global mine-count weighting: weight configurations by
  `C(hidden non-frontier, remaining mines)` for exact endgame probabilities
- Full-board constraint solving for very large frontiers
- Headless mode for batch runs and solver win-rate statistics
- Move-count and win-rate tracking per game
- Difficulty selection and a "new game" reset wired to the solver

