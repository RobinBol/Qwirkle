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
    public boolean setStone(int x, int y, Stone stone) {
    	if (getStone(x, y) == null) {
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
    	
    	return false;
    }
    
    private void createTestMap() {
    	for (int i = 0; i < 6; i++) {
    		//test purpose only, is not valid according to game rules.
        	board.put(Coordinate.getCoordinateHash(i, 0), new Stone(Stone.getRandomShape(), Stone.getRandomColor())); 
		}
    }
}
