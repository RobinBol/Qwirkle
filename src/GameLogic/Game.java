package GameLogic;

import ServerClient.ClientHandler;

import java.util.ArrayList;

public class Game {
	
	public static final int MAXHANDSIZE = 6;
	
    private Player[] players;
    private Board board;

    public Game(ArrayList<ClientHandler> clients) {
    	
        System.out.println("Started a game with:");
        System.out.println(clients);
        
        players = new Player[clients.size()];
        board = new Board();
        
        for (int i = 0; i < players.length; i++) {
			players[i] = new Player(clients.get(i), board);
		}

        // TODO: create players from the clients here and start the game
        
    }
    
    public void startGame(){};
    public void setTurn(){};
    public int getScore(Player player){return 0;};
    public void terminateGame(){}; // Players back to lobby
    
    public Player getFirstPlayer() {
    	for (int i = 0; i < players.length; i++) {
    		
    	}
    	
    	return null;
    }
}
