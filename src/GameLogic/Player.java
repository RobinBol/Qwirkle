package GameLogic;

import ServerClient.Client;
import ServerClient.ClientHandler;
import Tests.TestTUI;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import QwirkleUI.TUIBasic;

public class Player {
    private String name;
    private Board board;
    public ClientHandler client;
    private Stone[] hand; //stones that are in the hand.
    
    private TUIBasic tui;
    
    public Player(ClientHandler clientHandler, Board board) {
    	this.client = clientHandler;
        this.name = clientHandler.getClientName();
        this.board = board;
        InitializeHand();
        tui = new TUIBasic(board);
    }
    
    public Player(Board board) {
        this.board = board;
        InitializeHand();
        tui = new TUIBasic(board);
    }
    
    public void doTurn() {
    	this.board.createTestMap();
    	tui.drawBoard();
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
    
    /*
     * Gets the longest line possible with the stones held in the hand.
     * TODO: not needed thus to large to implement now.
     */
    public int getLongestCombination() {
    	int longest = 0;
    	Stone startingStone;
    	List<Stone> foundShapes = new ArrayList<Stone>();
    	List<Stone> foundColors = new ArrayList<Stone>();
    	
    	for (int i = 0; i < hand.length; i++) {
    		startingStone = hand[i];
    		for (int j = 0; j < hand.length; j++) {
    			if (j != i) {
    				if (startingStone.getColor() == hand[j].getColor() && startingStone.getShape() != hand[j].getShape()) {
    					foundColors.add(hand[j]);
    				}
    				if (startingStone.getColor() != hand[j].getColor() && startingStone.getShape() == hand[j].getShape()) {
    					foundShapes.add(hand[j]);
    				}
    			}
    		}
    	}
    	return 0;
    }
    
    public void InitializeHand() {
    	hand = board.getFirstHand();
    }
    
    public Stone[] getHand() {
    	return hand;
    }
    
    public static void main(String[] args) {
    	Player player = new Player(new Board());
    	player.doTurn();
    }
}
