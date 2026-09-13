import java.awt.Color;
import java.util.*;
import javax.swing.JButton;

public class Grid extends JButton {

    //Attributes
    private int nearbyMines = 0;
    private boolean isRevealed = false;
    private boolean isFlagged = false;
    private boolean isMine = false;
    private Color originalColor;
    private int x;
    private int y;
    private int probability = 0;
    private boolean guessMines;

    //Neighbors
    private Grid North = null;
    private Grid East = null;
    private Grid NorthWest = null;
    private Grid SouthEast = null;
    private Grid NorthEast = null;
    private Grid South = null;
    private Grid West = null;
    private Grid SouthWest = null;

    //Getters
    public Grid getSouth(){
        return South;
    }
    public Grid getNorth(){
        return North;
    }
    public Grid getEast(){
        return East;
    }
    public Grid getNorthWest(){
        return NorthWest;
    }
    public Grid getSouthEast(){
        return SouthEast;
    }
    public Grid getNorthEast(){
        return NorthEast;
    }
    public Grid getWest(){
        return West;
    }
    public Grid getSouthWest(){
        return SouthWest;
    }
    public List<Grid> getCardinalDirections(){
        return Arrays.asList(this.South, this.North, this.East, this.West, this.NorthEast, this.SouthWest, this.NorthWest, this.SouthEast);
    }
    public int getNearbyMines() {
        return nearbyMines;
    }
    public boolean isRevealed() {
        return isRevealed;
    }
    public boolean isFlagged() {
        return isFlagged;
    }
    public boolean isMine() {
        return isMine;
    }
    public int getXCoord(){
        return x;
    }
    public int getYCoord(){
        return y;
    }
    public int getProbability(){
        return this.probability;
    }
    public boolean getGuessMines(){
        return this.guessMines;
    }

    //Setters
    public void setNorth(Grid north){
        this.North = north;
    }
    public void setEast(Grid east){
        this.East = east;
    }
    public void setNorthWest(Grid northWest){
        this.NorthWest = northWest;
    }
    public void setSouthEast(Grid SouthEast){
        this.SouthEast = SouthEast;
    }
    public void setNorthEast(Grid NorthEast){
        this.NorthEast = NorthEast;
    }
    public void setSouth(Grid south){
        this.South = south;
    }
    public void setWest(Grid west){
        this.West = west;
    }
    public void setSouthWest(Grid SouthWest){
        this.SouthWest = SouthWest;
    }
    
    public void setNearbyMines(int nearbyMines) {
        this.nearbyMines = nearbyMines;
    }
    public void setRevealed() {
        this.isRevealed = true;
    }
    public void setFlagged() {
        this.isFlagged = !this.isFlagged;
    }
    public void setMine() {
        this.isMine = true;
    }
    public void setColor(Color color){
        this.originalColor = color;
        this.setBackground(this.originalColor);
    }
    public void setOriginalColor(){
        this.setBackground(this.originalColor);
    }
    public void setXCoord(int x){
        this.x = x;
    }
    public void setYCoord(int y){
        this.y = y;
    }
    public void setProbability(int probability){
        this.probability = probability;
    }
    public void setGuessMines(){
        this.guessMines = true;
    }

    

    public void reset() {
        this.nearbyMines = 0;
        this.isRevealed = false;
        this.isFlagged = false;
        this.isMine = false;
    }
}
