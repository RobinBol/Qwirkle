public class Game {
    private Players[] players;

    public Game() {
        startGame();
    }
    public void startGame();
    public void setTurn();
    public int getScore(Player player);
    public void terminateGame(); // Players back to lobby

}
