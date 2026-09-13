
public class App {
    public static void main(String[] args) throws Exception {
        //Game game = new Game();
        
        Computer computer = new Computer();

        int ticker = 0;

    while (true) {
            computer.findBombs();
            computer.clearTiles();
            if (ticker % 4 == 3){
                computer.setTheories();
            }
            ticker++;
            Thread.sleep(1000);
        }
    }
}
