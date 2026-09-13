public class App {
    public static void main(String[] args) throws Exception {
        Computer computer = new Computer();
        int ticker = 0;

        while (true) {
            // ---- STAGE 1: default deduction ----
            computer.findBombs();
            computer.clearTiles();

            // ---- STAGE 2: set theory (every 4th tick) ----
            if (ticker % 4 == 3) {
                computer.setTheories();
            }

            // ---- STAGE 3: probabilistic fallback ----
            // Only fires when stages 1 and 2 found nothing.
            // guess() re-checks; if it can't help either, it returns false.
            if (!computer.step()) {
                computer.guess();
            }

            ticker++;
            Thread.sleep(1000);
        }
    }
}
