import java.awt.*;
import java.util.*;

public class Computer extends Game {

    // ============================================================
    //  STAGE 1 — DEFAULT DEDUCTION
    //  Two rules, applied every cycle:
    //    findBombs : if a number equals its hidden-neighbor count,
    //                every hidden neighbor is a mine → flag them.
    //    clearTiles: if a number equals its flagged-neighbor count,
    //                every remaining hidden neighbor is safe → reveal.
    // ============================================================

    public void findBombs() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (grid[row][col].isRevealed()) {
                    findBomb(row, col);
                }
            }
        }
    }

    private void findBomb(int x, int y) {
        int iMin = Math.max(0, x - 1);
        int iMax = Math.min(rows - 1, x + 1);
        int jMin = Math.max(0, y - 1);
        int jMax = Math.min(cols - 1, y + 1);

        int unrevealed = 0;
        for (int i = iMin; i <= iMax; i++) {
            for (int j = jMin; j <= jMax; j++) {
                if (!grid[i][j].isRevealed()) unrevealed++;
            }
        }

        if (grid[x][y].getNearbyMines() == unrevealed) {
            for (int i = iMin; i <= iMax; i++) {
                for (int j = jMin; j <= jMax; j++) {
                    if (!grid[i][j].isRevealed() && !grid[i][j].isFlagged()) {
                        grid[i][j].setFlagged();
                        grid[i][j].setBackground(Color.BLUE);
                        totalFlags--;
                    }
                }
            }
        }
    }

    public void clearTiles() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (grid[row][col].isRevealed()) {
                    clearTile(row, col);
                }
            }
        }
    }

    private void clearTile(int x, int y) {
        int iMin = Math.max(0, x - 1);
        int iMax = Math.min(rows - 1, x + 1);
        int jMin = Math.max(0, y - 1);
        int jMax = Math.min(cols - 1, y + 1);

        int flagged = 0, unrevealed = 0;
        for (int i = iMin; i <= iMax; i++) {
            for (int j = jMin; j <= jMax; j++) {
                if (!grid[i][j].isRevealed()) unrevealed++;
                if (grid[i][j].isFlagged()) flagged++;
            }
        }

        if (flagged == grid[x][y].getNearbyMines()
                && unrevealed > grid[x][y].getNearbyMines()) {
            for (int i = iMin; i <= iMax; i++) {
                for (int j = jMin; j <= jMax; j++) {
                    if (!grid[i][j].isRevealed() && !grid[i][j].isFlagged()) {
                        removeTile(i, j);
                    }
                }
            }
        }
    }

    // ============================================================
    //  STAGE 2 — SET THEORY
    //  Pairwise subset deduction. For two revealed numbered tiles,
    //  if one has strictly more mines than the other and the
    //  difference equals the size of its exclusive neighbor group,
    //  then the exclusive tiles are all mines and the other side's
    //  exclusive tiles are all safe (or vice versa).
    // ============================================================

    public void setTheories() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                setTheory(row, col);
            }
        }
    }

    private void setTheory(int row, int col) {
        if (!grid[row][col].isRevealed() || grid[row][col].getNearbyMines() == 0) {
            return;
        }

        int originalMines = grid[row][col].getNearbyMines();
        ArrayList<Grid> originalGroup = new ArrayList<>();
        ArrayList<Grid> possiblePair = new ArrayList<>();

        for (Grid dir : grid[row][col].getCardinalDirections()) {
            if (dir == null) continue;
            if (dir.isFlagged()) {
                originalMines--;
            } else if (!dir.isRevealed()) {
                originalGroup.add(dir);
            } else if (dir.getNearbyMines() != 0) {
                possiblePair.add(dir);
            }
        }

        if (originalGroup.isEmpty() || possiblePair.isEmpty()) return;

        for (Grid pairTile : possiblePair) {
            int pairMines = pairTile.getNearbyMines();
            ArrayList<Grid> pairGroup = new ArrayList<>();
            ArrayList<Grid> pairExclusive = new ArrayList<>();
            ArrayList<Grid> originalExclusive = new ArrayList<>();

            for (Grid dir : pairTile.getCardinalDirections()) {
                if (dir == null) continue;
                if (dir.isFlagged()) {
                    pairMines--;
                } else if (!dir.isRevealed()) {
                    pairGroup.add(dir);
                    if (!originalGroup.contains(dir)) pairExclusive.add(dir);
                }
            }

            for (Grid o : originalGroup) {
                if (!pairGroup.contains(o)) originalExclusive.add(o);
            }

            if (originalMines > pairMines) {
                if (originalMines - pairMines == originalExclusive.size()
                        && !originalExclusive.isEmpty()) {
                    for (Grid t : pairExclusive) {
                        removeTile(t.getXCoord(), t.getYCoord());
                    }
                    for (Grid t : originalExclusive) {
                        if (!t.isFlagged()) {
                            t.setFlagged();
                            t.setBackground(Color.PINK);
                            totalFlags--;
                        }
                    }
                }
            } else if (originalMines < pairMines) {
                if (pairMines - originalMines == pairExclusive.size()
                        && !pairExclusive.isEmpty()) {
                    for (Grid t : originalExclusive) {
                        removeTile(t.getXCoord(), t.getYCoord());
                    }
                    for (Grid t : pairExclusive) {
                        if (!t.isFlagged()) {
                            t.setFlagged();
                            t.setBackground(Color.PINK);
                            totalFlags--;
                        }
                    }
                }
            }
        }
    }

    // ============================================================
    //  STAGE 3 — PROBABILISTIC
    //  Only used when stages 1 and 2 have stalled (no guaranteed
    //  move exists). Computes an exact per-tile mine probability
    //  by enumerating every mine configuration consistent with the
    //  visible numbers, then makes the safest possible move.
    // ============================================================

    /**
     * Returns every hidden, unflagged tile that touches at least
     * one revealed tile. These are the only tiles we can reason
     * about from the visible board.
     */
    public ArrayList<Grid> edgeTiles() {
        ArrayList<Grid> edges = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Grid tile = grid[i][j];
                if (tile.isRevealed() || tile.isFlagged()) continue;
                for (Grid dir : tile.getCardinalDirections()) {
                    if (dir != null && dir.isRevealed()) {
                        edges.add(tile);
                        break;
                    }
                }
            }
        }
        return edges;
    }

    /**
     * Exact probability, tile by tile, via constraint enumeration.
     *
     *  1. Index the frontier.
     *  2. Build one constraint per revealed numbered tile:
     *         sum(mines in hidden neighbors) = number - flagged
     *  3. Union-find the frontier into independent components.
     *  4. Backtrack each component with pruning.
     *  5. Tally per-tile mine counts across valid configurations.
     *
     * Result stored on each Grid as an integer 0–100.
     */
    public void probability(ArrayList<Grid> frontier) {
        if (frontier == null || frontier.isEmpty()) return;

        int n = frontier.size();
        HashMap<Grid, Integer> index = new HashMap<>();
        for (int i = 0; i < n; i++) index.put(frontier.get(i), i);

        // --- Build constraints ---
        ArrayList<int[]> constraints = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Grid tile = grid[r][c];
                if (!tile.isRevealed() || tile.getNearbyMines() == 0) continue;

                int required = tile.getNearbyMines();
                ArrayList<Integer> hidden = new ArrayList<>();
                for (Grid nb : tile.getCardinalDirections()) {
                    if (nb == null) continue;
                    if (nb.isFlagged()) required--;
                    else if (!nb.isRevealed()) {
                        Integer idx = index.get(nb);
                        if (idx != null) hidden.add(idx);
                    }
                }
                if (hidden.isEmpty()) continue;
                if (required < 0 || required > hidden.size()) return;

                int[] con = new int[hidden.size() + 1];
                con[0] = required;
                for (int i = 0; i < hidden.size(); i++) con[i + 1] = hidden.get(i);
                constraints.add(con);
            }
        }

        if (constraints.isEmpty()) {
            for (Grid t : frontier) t.setProbability(50);
            return;
        }

        // --- Union-find into independent components ---
        boolean[] constrained = new boolean[n];
        for (int[] con : constraints)
            for (int i = 1; i < con.length; i++) constrained[con[i]] = true;

        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        for (int[] con : constraints)
            for (int i = 2; i < con.length; i++)
                union(parent, con[1], con[i]);

        HashMap<Integer, ArrayList<Integer>> compTiles = new HashMap<>();
        HashMap<Integer, ArrayList<int[]>> compCons = new HashMap<>();
        for (int i = 0; i < n; i++) {
            if (!constrained[i]) continue;
            int root = find(parent, i);
            compTiles.computeIfAbsent(root, k -> new ArrayList<>()).add(i);
        }
        for (int[] con : constraints) {
            int root = find(parent, con[1]);
            compCons.computeIfAbsent(root, k -> new ArrayList<>()).add(con);
        }

        // --- Enumerate each component ---
        for (Map.Entry<Integer, ArrayList<Integer>> e : compTiles.entrySet()) {
            int root = e.getKey();
            ArrayList<Integer> tiles = e.getValue();
            ArrayList<int[]> cons = compCons.get(root);
            if (cons == null || cons.isEmpty()) continue;

            int m = tiles.size();

            // Component too big to brute-force → local heuristic
            if (m > 22) {
                for (int idx : tiles) probabilityHeuristicOne(frontier.get(idx));
                continue;
            }

            // Remap indices locally
            HashMap<Integer, Integer> local = new HashMap<>();
            for (int i = 0; i < m; i++) local.put(tiles.get(i), i);

            ArrayList<int[]> localCons = new ArrayList<>();
            for (int[] con : cons) {
                int[] lc = new int[con.length];
                lc[0] = con[0];
                boolean ok = true;
                for (int i = 1; i < con.length; i++) {
                    Integer li = local.get(con[i]);
                    if (li == null) { ok = false; break; }
                    lc[i] = li;
                }
                if (ok) localCons.add(lc);
            }

            int[] assign = new int[m];
            Arrays.fill(assign, -1);
            double[] mineSum = new double[m];
            double[] count = new double[1];

            enumerate(0, m, assign, localCons, mineSum, count);

            double total = count[0];
            if (total == 0) {
                for (int idx : tiles) frontier.get(idx).setProbability(50);
                continue;
            }
            for (int i = 0; i < m; i++) {
                int pct = (int) Math.round((mineSum[i] / total) * 100.0);
                if (pct < 0) pct = 0;
                if (pct > 100) pct = 100;
                frontier.get(tiles.get(i)).setProbability(pct);
            }
        }

        // Unconstrained frontier tiles default to 50%
        for (int i = 0; i < n; i++) {
            if (!constrained[i]) frontier.get(i).setProbability(50);
        }
    }

    // ---- Backtracking enumerator ----
    private void enumerate(int pos, int m, int[] assign, ArrayList<int[]> cons,
                           double[] mineSum, double[] count) {
        if (pos == m) {
            for (int[] con : cons) {
                int sum = 0;
                for (int i = 1; i < con.length; i++) sum += assign[con[i]];
                if (sum != con[0]) return;
            }
            count[0] += 1.0;
            for (int i = 0; i < m; i++) if (assign[i] == 1) mineSum[i] += 1.0;
            return;
        }
        for (int v = 0; v <= 1; v++) {
            assign[pos] = v;
            if (partialOK(assign, cons)) {
                enumerate(pos + 1, m, assign, cons, mineSum, count);
            }
            assign[pos] = -1;
        }
    }

    // ---- Prune partial assignments that already violate a constraint ----
    private boolean partialOK(int[] assign, ArrayList<int[]> cons) {
        for (int[] con : cons) {
            int totalTiles = con.length - 1;
            int assignedMines = 0, assignedTiles = 0;
            boolean touched = false;

            for (int i = 1; i < con.length; i++) {
                if (assign[con[i]] == -1) continue;
                touched = true;
                assignedTiles++;
                if (assign[con[i]] == 1) assignedMines++;
            }
            if (!touched) continue;

            int required = con[0];
            if (assignedMines > required) return false;
            if (assignedMines + (totalTiles - assignedTiles) < required) return false;
        }
        return true;
    }

    // ---- Fallback heuristic for oversized components ----
    private void probabilityHeuristicOne(Grid tile) {
        double sum = 0; int count = 0;
        for (Grid nb : tile.getCardinalDirections()) {
            if (nb == null || !nb.isRevealed() || nb.getNearbyMines() == 0) continue;
            int remaining = nb.getNearbyMines();
            int hidden = 0;
            for (Grid n2 : nb.getCardinalDirections()) {
                if (n2 == null) continue;
                if (n2.isFlagged()) remaining--;
                else if (!n2.isRevealed()) hidden++;
            }
            if (hidden > 0) { sum += (double) remaining / hidden; count++; }
        }
        int pct = (count > 0) ? (int) Math.round((sum / count) * 100) : 50;
        tile.setProbability(Math.max(0, Math.min(100, pct)));
    }

    // ---- Union-find ----
    private int find(int[] parent, int x) {
        while (parent[x] != x) { parent[x] = parent[parent[x]]; x = parent[x]; }
        return x;
    }
    private void union(int[] parent, int a, int b) {
        int ra = find(parent, a), rb = find(parent, b);
        if (ra != rb) parent[rb] = ra;
    }

    // ============================================================
    //  PROBABILISTIC MOVE
    //  Uses the scores from probability() to make the safest move.
    //  Returns true if a move was made.
    // ============================================================
    public boolean guess() {
        ArrayList<Grid> edges = edgeTiles();
        if (edges.isEmpty()) return false;

        probability(edges);

        Grid safest = null;
        int lowest = Integer.MAX_VALUE;

        for (Grid tile : edges) {
            int p = tile.getProbability();
            if (p == 0) {
                tile.setGuessMines();
                removeTile(tile.getXCoord(), tile.getYCoord());
                return true;
            }
            if (p == 100 && !tile.isFlagged()) {
                tile.setFlagged();
                tile.setBackground(Color.PINK);
                totalFlags--;
                return true;
            }
            if (p < lowest) { lowest = p; safest = tile; }
        }
        if (safest != null) {
            safest.setGuessMines();
            removeTile(safest.getXCoord(), safest.getYCoord());
            return true;
        }
        return false;
    }
}
