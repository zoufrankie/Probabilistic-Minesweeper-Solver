# Minesweeper with Automated Solver

A Java implementation of Minesweeper featuring a Swing GUI and an automated
solver that plays the game using rule-based and subset-based deduction. Built
to explore constraint solving and game AI on a classic puzzle.

---

## Overview

The project has two parts:

1. **The game** (`Game.java`, `Grid.java`) — a playable 10×10 Minesweeper board
   with 20 mines, a timer, flagging, flood-fill reveal, and win/loss detection.
2. **The solver** (`Computer.java`) — an automated player that reads the board
   state, deduces mine locations, and flags or reveals tiles accordingly.

The solver runs in a loop (`App.java`), applying its deduction rules each cycle
until no further progress can be made.

---

## Features

### Game
- 10×10 grid with 20 mines (configurable via `rows`, `cols`, `totalMines`)
- Mines placed after the first click, guaranteeing a safe opening area
- Left-click to reveal, right-click (or Ctrl+left-click) to flag
- Flood-fill reveal for tiles with no adjacent mines
- Per-number color coding (1–8)
- Live timer and flags-remaining display
- Restart button and win/loss detection with mine reveal on game over

### Solver
- **Basic mine deduction** — when a revealed tile's number equals its count of
  unrevealed neighbors, every one of those neighbors is a mine
- **Basic safe deduction** — when a revealed tile's number equals its count of
  flagged neighbors, all remaining unrevealed neighbors are safe
- **Subset deduction** — compares pairs of numbered tiles to isolate exclusive
  neighbor groups and resolve mines or safe tiles by difference
- Applies all rules continuously, escalating to subset reasoning on a timer

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

### 1. `findBombs()` — Basic mine deduction

For every revealed tile, the solver counts its unrevealed neighbors. If that
count equals the tile's number, all those neighbors must be mines, so they are
flagged.

```
if (tile.number == unrevealedNeighbors) → flag all unrevealed neighbors
```

### 2. `clearTiles()` — Basic safe deduction

For every revealed tile, the solver counts its flagged neighbors. If that count
equals the tile's number, all other unrevealed neighbors are safe, so they are
revealed.

```
if (tile.number == flaggedNeighbors) → reveal all other unrevealed neighbors
```

### 3. `setTheories()` — Subset deduction

For each revealed numbered tile, the solver compares it against each neighboring
numbered tile:

1. Build each tile's set of unrevealed, unflagged neighbors.
2. Subtract the two sets to find each tile's **exclusive** group.
3. If `minesA > minesB` and `(minesA - minesB) == size(A exclusive)`, then A's
   exclusive tiles are all mines and B's exclusive tiles are all safe (and vice
   versa).

This resolves classic patterns such as 1-2-1 and 1-2-2-1 that the basic rules
alone cannot solve.

**Scheduling:** `findBombs()` and `clearTiles()` run every cycle, while
`setTheories()` runs every fourth cycle (`App.java`).

---

## How to Run

**Requirements:** Java 8 or later (uses `java.awt` and `javax.swing`).

```bash
javac *.java
java App
```

`App` launches the solver loop against the board automatically. To play
manually instead, uncomment `Game game = new Game();` in `App.java` and comment
out the solver block.

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
- **Expert:** 24×20, 99 mines (already sketched in the source comments)

---

## Design Notes

- **Neighbor links over index math.** Each `Grid` tile holds direct references
  to its eight neighbors, set once during board construction. The solver can
  then traverse the board without repeated bounds checking.
- **Deduction before guessing.** Every rule the solver applies is provably
  correct, so tiles are only revealed when they are guaranteed safe. This keeps
  the solver from losing to avoidable mistakes.
- **Layered rule strength.** The basic rules are cheap and catch most moves;
  the subset rule is more expensive but resolves the patterns the basics miss.

---

## Future Enhancements

- Probabilistic guessing, weighted by the number of mines remaining, for boards
  where no logical move exists
- Global constraint solving via full-board enumeration
- Perimeter-based reasoning to reduce the search space on larger boards
- Headless mode for batch runs and solver win-rate statistics
- Move-count and win-rate tracking per game

---

## License

Released under the MIT License. See `LICENSE` for details.
