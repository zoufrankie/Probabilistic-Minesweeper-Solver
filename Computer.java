import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;

public class Computer extends Game {

    public void findBombs(){
        for (int row = 0; row < rows; row++){
            for(int col = 0; col < cols; col++){
                if (grid[row][col].isRevealed()) {
                    findBomb(row, col);
                }
            }
        }
    }
    
    private void findBomb(int x, int y){
        int iMin = Math.max(0, x - 1);
        int iMax = Math.min(rows - 1, x + 1);
        int jMin = Math.max(0, y - 1);
        int jMax = Math.min(cols - 1, y + 1);
        int unclearedTiles = 0;
            for (int i = iMin; i <= iMax; i++) {
                for (int j = jMin; j <= jMax; j++) {
                    if (!grid[i][j].isRevealed()){
                        unclearedTiles++;
                    }
                }
            }
            if (grid[x][y].getNearbyMines() == unclearedTiles){
                for (int i = iMin; i <= iMax; i++) {
                    for (int j = jMin; j <= jMax; j++) {
                        if (!grid[i][j].isRevealed() && !grid[i][j].isFlagged()){
                            grid[i][j].setFlagged();
                            grid[i][j].setBackground(Color.BLUE);
                            totalFlags--;
                        }
                    }
                }
                
            }
    }
    
    public void clearTiles(){
        for (int row = 0; row < rows; row++){
            for(int col = 0; col < cols; col++){
                if (grid[row][col].isRevealed()) {
                    clearTile(row, col);
                }
            }
        }
    }
    
    private void clearTile(int x, int y){
        int iMin = Math.max(0, x - 1);
        int iMax = Math.min(rows - 1, x + 1);
        int jMin = Math.max(0, y - 1);
        int jMax = Math.min(cols - 1, y + 1);
        int minesFlagged = 0;
        int unclearedTiles = 0;
        for (int i = iMin; i <= iMax; i++) {
            for(int j = jMin; j <= jMax; j++) {
                if (!grid[i][j].isRevealed()){
                    unclearedTiles++;
                }
                if (grid[i][j].isFlagged()) {
                    minesFlagged++;
                }
            }
        }
        if (minesFlagged == grid[x][y].getNearbyMines() && unclearedTiles > grid[x][y].getNearbyMines()) {
            for (int i = iMin; i <= iMax; i++) {
                for(int j = jMin; j <= jMax; j++) {
                    if (!grid[i][j].isRevealed() && !grid[i][j].isFlagged()) {
                        removeTile(i, j);
                    }
                }
            }
        }
    }

    public void setTheories(){
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                setTheory(row, col);
            }
        }
    }

    private void setTheory(int row, int col){
        if (grid[row][col].isRevealed() && grid[row][col].getNearbyMines() != 0) {
            int localMineOriginal = grid[row][col].getNearbyMines();

            ArrayList<Grid> possiblePair = new ArrayList<Grid>();

            ArrayList<Grid> originalGroup = new ArrayList<Grid>();

            for (Grid directions : grid[row][col].getCardinalDirections()) {
                if (directions != null) {
                    if (!directions.isRevealed() && !directions.isFlagged()){
                        originalGroup.add(directions);
                    }
                    if (directions.isRevealed() && directions.getNearbyMines() != 0){
                        possiblePair.add(directions);
                    }
                }
                if (directions != null && directions.isFlagged()){
                    localMineOriginal--;
                }
            }

            if (originalGroup.isEmpty() || possiblePair.isEmpty() || originalGroup.isEmpty()){
                return;
            }

            // possiblePair is now in scope here
            for (int i = 0; i < possiblePair.size(); i++){
                ArrayList<Grid> pairGroup = new ArrayList<Grid>();
                int localMinePair = possiblePair.get(i).getNearbyMines();

                ArrayList<Grid> originalExclusiveGroup = new ArrayList<Grid>();
                ArrayList<Grid> pairExclusiveGroup = new ArrayList<Grid>();
                
                for (Grid directions : possiblePair.get(i).getCardinalDirections()) {
                    if (directions != null && !directions.isRevealed() && !directions.isFlagged()) {
                        pairGroup.add(directions);
                        if (!originalGroup.contains(directions)){
                            pairExclusiveGroup.add(directions);
                        }
                    }
                    if (directions != null && directions.isFlagged()){
                        localMinePair--;
                    }
                }

                for (Grid oUnrevealed : originalGroup){
                    if (!pairGroup.contains(oUnrevealed)){
                        originalExclusiveGroup.add(oUnrevealed);
                    }
                }
                
                if (localMineOriginal > localMinePair){
                    if (localMineOriginal - localMinePair == originalExclusiveGroup.size() && !originalExclusiveGroup.isEmpty()) {
                        for (Grid revealThis : pairExclusiveGroup){
                            removeTile(revealThis.getXCoord(), revealThis.getYCoord());
                        }
                        for (Grid flagThat : originalExclusiveGroup){
                            if (!flagThat.isFlagged()) {
                            flagThat.setFlagged();
                            flagThat.setBackground(Color.PINK);
                            totalFlags--;
                            }
                        }
                    }
                }
                else if (localMineOriginal < localMinePair) {
                    if (localMinePair - localMineOriginal == pairExclusiveGroup.size() && !pairExclusiveGroup.isEmpty()) {
                        for (Grid revealThis : originalExclusiveGroup){
                            removeTile(revealThis.getXCoord(), revealThis.getYCoord());
                        }
                        for (Grid flagThat : pairExclusiveGroup){
                            if (!flagThat.isFlagged()) {
                                flagThat.setFlagged();
                                flagThat.setBackground(Color.PINK);
                                totalFlags--;
                            }
                        }
                    }
                }

            }
        
        }
    }
    private boolean isNextToNumberedTile(Grid tile){
        for (Grid directions : tile.getCardinalDirections()) {
            if (directions != null && directions.isRevealed() && directions.getNearbyMines() != 0){
                return true;
            }
        }
        return false;
    }
    private ArrayList edgeTiles(){
        ArrayList<Grid> edgeTiles = new ArrayList<Grid>();
        for(int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                if (!grid[i][j].isRevealed()){
                    for (Grid directions : grid[i][j].getCardinalDirections()) {
                        if (directions != null && directions.isRevealed()){
                            edgeTiles.add(grid[i][j]);
                            break;
                        }
                    }
                }
            }
        }
        int i = edgeTiles.get(0).getX();
        int j = edgeTiles.get(0).getY();
        while (true) {
            if (!grid[i][j].getEast().isRevealed() && !grid[i][j].getEast().isFlagged() && isNextToNumberedTile(grid[i][j].getEast())) {
                edgeTiles.add(grid[i][j].getEast());
                j++;
            } else if (true) {
                
            } else if (true) {
                
            } else if (true) {
                
            } else if (true) {
                
            } else if (true) {
                
            } else if (true) {
                
            } else if (true) {
                
            } else if (i + 1 == rows || j + 1 == cols || i - 1 < 0 || j - 1 < 0) {
                break;
            }
        }
        return edgeTiles;
    }
    private void probability(ArrayList<Grid> edgeTiles){
        
    }
}
