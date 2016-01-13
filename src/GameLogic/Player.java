package GameLogic;

import ServerClient.Client;
import ServerClient.ClientHandler;

import java.net.InetAddress;

public class Player {
    private String name;
    public Player(ClientHandler clientHandler) {
        this.name = clientHandler.getClientName();
    }

    public String getName(){
        return this.name;
    }
    //boolean in case makeMove didn't execute well.
    public boolean makeMove() {
    	return false;
    }
    
    //only if board is empty.
    public Stone[] tradeStones(Stone [] stones) {
    	return null;
    }
    
    public void skipTurn() {
    	
    }
}
