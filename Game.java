import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Game {
    boolean start = true;
    boolean gameOver = false;
    double elapsedTime = 0;

    // int totalMines = 99;
    // int totalFlags = 99;

    // final int rows  = 20;
    // final int cols = 24;
    
    final int tileSize = 30;
    
    int totalMines = 20;
    int totalFlags = 20;

    final int rows  = 10;
    final int cols = 10;


    JFrame frame = new JFrame("MineSweeper");
    JPanel timerPanel = new JPanel();
    JPanel explanationPanel = new JPanel();
    JButton restartButton = new JButton("Restart");
    JPanel boardPanel = new JPanel();

    Grid[][] grid = new Grid[rows][cols];
    
    Timer gameTimer;
    JLabel timerLabel = new JLabel("Time: 0 seconds");
    Game() {

        // ===== Timer Panel =====
        timerPanel.setBackground(Color.lightGray);
        timerPanel.setPreferredSize(new Dimension(cols * tileSize, 50));
        timerLabel.setFont(new Font("Arial", Font.BOLD, 40));
        timerLabel.setForeground(Color.white);
        timerPanel.add(timerLabel);

        // ===== Explanation Panel =====
        explanationPanel.setBackground(Color.lightGray);
        explanationPanel.setPreferredSize(new Dimension(cols * tileSize, 150));
        JLabel explanationLabel = new JLabel("Click on a tile to start the game!");
        explanationLabel.setFont(new Font("Arial", Font.BOLD, 20));
        explanationLabel.setForeground(Color.white);
        explanationPanel.add(explanationLabel);

        // ===== Game Grid Setup =====
        explanationPanel.add(restartButton);
        restartButton.setFont(new Font("Arial", Font.BOLD, 20));
        restartButton.setVisible(false);

        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Reset game state
                start = true;
                gameOver = false;
                elapsedTime = 0;
                totalFlags = totalMines;
                timerLabel.setText("Time: 0 seconds");
                explanationLabel.setText("Click on a tile to start the game!");
                restartButton.setVisible(false);
                gameTimer.stop();

                // Reset grid and tiles
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        grid[i][j].reset();
                        grid[i][j].setEnabled(true);
                        grid[i][j].setText("");
                        if ((i + j) % 2 == 0) {
                            grid[i][j].setBackground(new Color(76, 175, 80)); //Slighty dark green
                        } else {
                            grid[i][j].setBackground(new Color(167, 216, 161)); //Lighter green
                        }
                    }
                }
            }
        });

        boardPanel.setLayout(new GridLayout(rows, cols));

        // Initialize grid logic cells
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Grid();
            }
        }

        // Initialize tiles (buttons)
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Grid tile = new Grid();
                grid[i][j] = tile;

                tile.setXCoord(i);
                tile.setYCoord(j);
                tile.setFont(new Font("Arial", Font.BOLD, 16));
                tile.setMargin(new Insets(0, 0, 0, 0));
                tile.setFocusPainted(false);
                tile.setBorder(BorderFactory.createLineBorder(new Color(60, 120, 60), 1));
                tile.setContentAreaFilled(true);
                tile.setOpaque(true);

                // Alternate tile background color
                if ((i + j) % 2 == 0) {
                    tile.setColor(new Color(76, 175, 80)); // Slightly dark green
                } else {
                    tile.setColor(new Color(167, 216, 161)); // Lighter green
                }

                boardPanel.add(tile);

                final int row = i;
                final int col = j;

                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e) && !e.isControlDown()) {
                            if (start && !grid[row][col].isFlagged()) {
                                start = false;
                                placeMines(row, col);
                                gameTimer.start();
                            }
                            if(!gameOver && !grid[row][col].isFlagged()){
                            removeTile(row, col);
                            if(grid[row][col].isMine()){
                                gameTimer.stop();
                                gameOver = true;
                                explanationLabel.setText("You Lose! Time: " + (int) elapsedTime + " seconds");
                                restartButton.setVisible(true);
                                revealMines();
                                
                            }
                            else if(win()){
                                gameTimer.stop();
                                gameOver = true;
                                explanationLabel.setText("You Win! Time: " + (int) elapsedTime + " seconds");
                                restartButton.setVisible(true);
                                revealMines();
                            }
                            }
                        }
                        // Optional: add right-click flag logic here
                        if (SwingUtilities.isRightMouseButton(e) || SwingUtilities.isLeftMouseButton(e) && e.isControlDown()){
                            if(grid[row][col].isRevealed() == false && !gameOver){
                            if(!grid[row][col].isFlagged()){
                            grid[row][col].setFlagged();
                            grid[row][col].setBackground(Color.BLUE);
                            totalFlags--;
                            }
                            else {
                                grid[row][col].setOriginalColor();
                                grid[row][col].setFlagged();
                                totalFlags++;
                            }
                        }
                        }
                    }
                });
            }
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i-1 >= 0){
                    grid[i][j].setNorth(grid[i-1][j]);
                    if (j-1 >= 0) {
                        grid[i][j].setNorthWest(grid[i-1][j-1]);
                    }
                    if (j+1 < cols) {
                        grid[i][j].setNorthEast(grid[i-1][j+1]);
                    }
                }
                if (i+1 < rows){
                    grid[i][j].setSouth(grid[i+1][j]);
                    if (j-1 >= 0) {
                        grid[i][j].setSouthWest(grid[i+1][j-1]);
                    }
                    if(j+1 < cols){
                        grid[i][j].setSouthEast(grid[i+1][j+1]);
                    }
                }
                
                if (j-1 >= 0){
                    grid[i][j].setWest(grid[i][j-1]);
                }
                
                if (j + 1 < cols) {
                    grid[i][j].setEast(grid[i][j+1]);
                }
                
 
                
            }
        }

        // ===== Timer Logic =====
        gameTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameOver) {
                    elapsedTime += .1;
                    timerLabel.setText("Time: " + (int) elapsedTime + " seconds");
                    explanationLabel.setText("Flags remaining: " + totalFlags);
    
                }
            }
        });
        
        // ===== Frame Setup =====
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        frame.add(timerPanel, BorderLayout.NORTH);
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(explanationPanel, BorderLayout.SOUTH);

        frame.setSize(cols * tileSize, rows * tileSize + 200); // Automatically size to fit components
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setResizable(false);
        frame.setVisible(true); // FINAL step (prevents squished layout)
    }

    // ===== Place Mines After First Click =====
    private void placeMines(int startRow, int startCol) {
        int minesPlaced = 0;

        while (minesPlaced < totalMines) {
            int x = (int) (Math.random() * rows);
            int y = (int) (Math.random() * cols);
            ArrayList<Integer> startRows = new ArrayList<Integer> (Arrays.asList(startRow-2, startRow-1, startRow, startRow+1, startRow+2));
            ArrayList<Integer> startCols = new ArrayList<Integer> (Arrays.asList(startCol-2, startCol-1, startCol, startCol+1, startCol+2));
            
            // Skip first clicked tile and already mined ones
            if (!(startRows.contains(x) && startCols.contains(y)) && !grid[x][y].isMine()) {
            grid[x][y].setMine();
            //grid[x][y].setBackground(Color.RED); // For testing purposes, show where mines are placed
            minesPlaced++;
            int iMin = Math.max(0, x - 1);
            int iMax = Math.min(rows - 1, x + 1);
            int jMin = Math.max(0, y - 1);
            int jMax = Math.min(cols - 1, y + 1);
            for (int i = iMin; i <= iMax; i++) {
                for (int j = jMin; j <= jMax; j++) {
                    grid[i][j].setNearbyMines(grid[i][j].getNearbyMines() + 1);
                    //grid[i][j].setText(Integer.toString(grid[i][j].getNearbyMines()));
                    if(grid[i][j].getNearbyMines() > 0){
                //grid[i][j].setText(Integer.toString(grid[i][j].getNearbyMines()));
                switch (grid[i][j].getNearbyMines()) {
                    case 1:
                        grid[i][j].setForeground(Color.BLUE);
                        break;
                    case 2:
                        grid[i][j].setForeground(new Color(0, 105, 0)); // Dark Green
                        break;
                    case 3:
                        grid[i][j].setForeground(Color.RED);
                        break;
                    case 4:
                        grid[i][j].setForeground(new Color(0, 0, 128)); // Dark Blue
                        break;
                    case 5:
                        grid[i][j].setForeground(new Color(178, 34, 34)); // Firebrick
                        break;
                    case 6:
                        grid[i][j].setForeground(new Color(72, 209, 204)); // Medium Turquoise
                        break;
                    case 7:
                        grid[i][j].setForeground(Color.BLACK);
                        break;
                    case 8:
                        grid[i][j].setForeground(Color.DARK_GRAY);
                        break;
                    default:
                        grid[i][j].setForeground(Color.BLACK);
                        break;
                }
            }
                }
            }
            for (int i = 0; i < rows; i++) {
                for(int j = 0; j < cols; j++){
                    if (grid[i][j].isMine()) {
                        grid[i][j].setNearbyMines(-1);
                        grid[i][j].setForeground(Color.white);
                        grid[i][j].setText("");
                    }
                }
            }
        }
    }
}

    //Method checks if the user wins
    private void revealMines(){
    for(int i = 0; i < rows; i++){
        for(int j = 0; j < cols; j++){
            if(grid[i][j].isMine()){
                grid[i][j].setBackground(Color.RED);
                grid[i][j].setText("M");
            }
        }
    }
}
    private boolean win(){
    for(int i = 0; i < rows; i++){
        for(int j = 0; j < cols; j++){
            if(!grid[i][j].isMine() && !grid[i][j].isRevealed()){
                return false;
            }

            
        }
    }
    return true;
}
    public void removeTile(int x, int y){
        if(grid[x][y].isMine()){
            gameTimer.stop();
            gameOver = true;
        } else {
            grid[x][y].setRevealed();
            Color tan = new Color(220, 190, 150);
            Color darkerTan = new Color(200, 170, 130);
            if ((x + y) % 2 == 0) {
                grid[x][y].setBackground(tan);
            } else {
                grid[x][y].setBackground(darkerTan);
            }
            if (grid[x][y].getNearbyMines() > 0) {
                        grid[x][y].setText(Integer.toString(grid[x][y].getNearbyMines()));
                    }
            int iMin = Math.max(0, x - 1);
            int iMax = Math.min(rows - 1, x + 1);
            int jMin = Math.max(0, y - 1);
            int jMax = Math.min(cols - 1, y + 1);
            for (int i = iMin; i <= iMax; i++) {
                for (int j = jMin; j <= jMax; j++) {
                    if(grid[i][j].isRevealed() == false && !(i == x && j == y) && grid[x][y].getNearbyMines() == 0){
                        removeTile(i, j);
                        if(grid[i][j].isFlagged()){
                            grid[i][j].setFlagged();
                            totalFlags++;
                        }
                    }
                }
            }
        }
    }
}
