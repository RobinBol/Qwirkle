package GameLogic;

import ServerClient.Client;

import java.net.InetAddress;

/**
 * Created by Robin on 11-01-16.
 */
public class Player extends Client {
    public Player(String name, InetAddress host, int port) {
        super(name, host, port);
    }
    
    
    //boolean in case makeMove didn't execute well.
    public boolean makeMove() {
    	return false;
    }
}
