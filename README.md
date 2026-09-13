# Minesweeper with Automated Solver

A Java implementation of Minesweeper with a Swing GUI and an automated solver
that plays the game using rule-based and subset-based deduction. Built as a
project exploring constraint solving and game AI.

---

## Overview

This project has two parts:

1. **The game** (`Game.java`, `Grid.java`) — a playable 10×10 Minesweeper board
   with 20 mines, a timer, flagging, flood-fill reveal, and win/loss detection.
2. **The solver** (`Computer.java`) — an automated player that reads the board
   state and deduces where mines are, then flags or reveals tiles accordingly.

The solver runs on a loop (`App.java`), repeatedly applying deduction rules
until no further progress is possible.

---

## Features

**Game**
- 10×10 grid with 20 mines (configurable via `rows`, `cols`, `totalMines`)
- Mines placed after the first click, with a safe zone around the starting tile
- Left-click to reveal, right-click (or Ctrl+left-click) to flag
- Flood-fill reveal for empty tiles
- Per-number color coding (1–8)
- Timer, flags-remaining display, restart button
- Win/loss detection with mine reveal on game over

**Solver**
- **Basic deduction** — if a revealed tile's number equals its count of
  unrevealed neighbors, all those neighbors are mines
- **Complementary deduction** — if a revealed tile's number equals its count of
  flagged neighbors, all remaining unrevealed neighbors are safe
- **Subset / theory deduction** — compares pairs of numbered tiles to find
  exclusive groups and deduce mines or safe tiles by difference
- Runs continuously, applying all rules each cycle

---

## Project Structure
├── App.java ← Entry point; runs the solver loop
├── Game.java ← Game logic, GUI, board setup, mine placement
├── Computer.java ← Automated solver (extends Game)
└── Grid.java ← Tile class (extends JButton); state + neighbor links

---

## How the Solver Works

### 1. `findBombs()` — Basic mine deduction

For every revealed tile, count its unrevealed neighbors. If that count equals
the tile's number, all those neighbors must be mines → flag them.
if (tile.number == unrevealedNeighbors) → flag all unrevealed neighbors

### 2. `clearTiles()` — Basic safe deduction

For every revealed tile, count its flagged neighbors. If that count equals the
tile's number, all other unrevealed neighbors are safe → reveal them.
if (tile.number == flaggedNeighbors) → reveal all other unrevealed neighbors

### 3. `setTheories()` — Subset deduction

For each revealed numbered tile, compare it against each neighboring numbered tile:

- Build each tile's set of unrevealed, unflagged neighbors.
- Subtract the two sets to find each tile's **exclusive** group.
- If `minesA > minesB` and `(minesA - minesB) == size(A exclusive)`, then A's
  exclusive tiles are all mines, and B's exclusive tiles are all safe (and vice versa).

This handles classic patterns like 1-2-1 and 1-2-2-1 that the basic rules miss.

The solver calls `findBombs()` and `clearTiles()` every tick, and
`setTheories()` every 4th tick (`App.java`).

---

## How to Run

**Requirements:** Java 8 or later (uses `java.awt`, `javax.swing`).

```bash
javac *.java
java App
This launches the solver loop. To play manually instead, uncomment
Game game = new Game(); in App.java and comment out the solver block.
Configuration
Edit these constants in Game.java:

Constant	Default	Meaning
rows	10	Board height
cols	10	Board width
totalMines	20	Number of mines
tileSize	30	Tile size in pixels
To match classic difficulty presets:

Beginner: 9×9, 10 mines

Intermediate: 16×16, 40 mines

Expert: 24×20, 99 mines

Known Limitations / Work in Progress
edgeTiles() is incomplete — the traversal loop contains placeholder
else if (true) branches and does not yet build the board edge correctly.
It also has no null check on edgeTiles.get(0), which can throw if the list
is empty.

probability() is an empty stub — probabilistic guessing is not
implemented, so the solver will stall on boards that require a guess.

The solver only reasons about tiles adjacent to numbered tiles; no global
constraint solving or endgame analysis yet.

App.java runs the solver on a fixed 1-second loop rather than stepping
after each move; timing is not tied to actual board updates.

Roadmap
Finish edge-tile enumeration for perimeter-based reasoning

Implement probabilistic guessing (weighted by remaining mine count)

Add global constraint solving (full board enumeration)

Add a headless mode for batch testing solver win rates

Track and report solver performance (games won, moves per game)
