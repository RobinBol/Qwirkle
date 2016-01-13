package GameLogic;

import java.util.HashMap;
import java.util.Map;

public class Board {
    private Bag bag;
    private Map<Integer, Stone> board;
    

    public Board() {
    	board = new HashMap<>();
    }
    
    /* Places a stone if possible.
     * 
     */
    public boolean placeStone(int x, int y, Stone stone) {
    	if (isEmptySpot(x, y)) {
    		board.put(Coordinate.getCoordinateHash(x, y), stone);
    		return true;
    	}    	
    	return false;
    }
    
    public Stone getStone(int x, int y) {
    	return board.get(Coordinate.getCoordinateHash(x, y));
    }
    
    
    /* Makes a move.
     * validates if the move is possible.
     * then places a stone at given position.
     */
    public boolean makeMove(int x, int y, Stone stone) {
    	if (makeMove(x, y, stone.getShape(), stone.getColor())) {
    		return true;
    	}
    	return false;
    }
    
    public boolean makeMove(int x, int y, char shape, char color) {
    	//if it is the first move on the board.
    	if (isEmptyBoard()) {
    		if (x == 0 && y == 0) {
    			//TODO: Not sure if the new stone might create duplicate cases. Make sure this is addressed in player.
    			placeStone(x, y, new Stone(shape, color));
    		}
    		//not the first tile on 0,0;
    		//TODO: Announce to player that it is an invalid move for the first turn?
    		//Or just do this in the player object.
    		
    		//System.out.println("Invalid first move");
    		return false;
    	}
    	
    	
    	return false;
    }
    
    
    public boolean isEmptyBoard() {
    	return board.isEmpty();
    }
    
    public boolean isEmptySpot(int x, int y) {
    	if (getStone(x, y) == null) {
    		return true;
    	} 
    	return false;
    }
    
    /*
     * returns if a position is next to an alreadplaced stone.
     */
    public boolean isConnected(int x, int y) {
    	return false;
    }
    
    private void createTestMap() {
    	for (int i = 0; i < 6; i++) {
    		//test purpose only, is not valid according to game rules.
        	board.put(Coordinate.getCoordinateHash(i, 0), new Stone(Stone.getRandomShape(), Stone.getRandomColor())); 
		}
    }
}
