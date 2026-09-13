while (true) {
    boolean changed = false;

    int f0 = totalFlags;
    int r0 = countRevealed();
    computer.findBombs();
    computer.clearTiles();
    if (ticker % 4 == 3) computer.setTheories();
    if (totalFlags != f0 || countRevealed() != r0) changed = true;

    if (!changed) {
        computer.probability(computer.edgeTiles());
        computer.guess();
    }

    ticker++;
    Thread.sleep(500);
}
