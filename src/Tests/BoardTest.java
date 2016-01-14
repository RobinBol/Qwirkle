package Tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import GameLogic.Board;
import GameLogic.Stone;

public class BoardTest {

	Board board;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		board.createTestMap();
	}

	@Test
	public void sameRowTest() {
		Stone[] valid = new Stone[] {new Stone('x', 'b', 0, 1), new Stone('x', 'b', 0, 2), new Stone('x', 'b', 0, 7)};
		Stone[] valid2 = new Stone[] {new Stone('x', 'b', 0, 0), new Stone('x', 'b', 1, 0), new Stone('x', 'b', 2, 0)};
		Stone[] inValid = new Stone[] {new Stone('x', 'b', 0, 1), new Stone('x', 'b', 0, 2), new Stone('x', 'b', 1, 2)};
		Stone[] inValid2 = new Stone[] {new Stone('x', 'b', 0, 1), new Stone('x', 'b', 0, 2), new Stone('x', 'b', 1, 7)};
		
		assertTrue(board.inSameRow(valid));
		assertTrue(board.inSameRow(valid2));
		assertFalse(board.inSameRow(inValid));
		assertFalse(board.inSameRow(inValid2));
	}

	@Test
	public void occupiedPlacesTest() {
		board.createTestMap();
		Stone[] valid = new Stone[] {new Stone('x', 'b', 1, 1), new Stone('x', 'b', 2, 2), new Stone('x', 'b', 3, 3)};
		Stone[] inValid = new Stone[] {new Stone('x', 'b', 0, 0), new Stone('x', 'b', 0, 2), new Stone('x', 'b', 1, 2)};
		//TestTUI tui = new TestTUI(board);
		//tui.drawBoard();
		assertFalse(board.takeOccupiedPlaces(valid));
		assertTrue(board.takeOccupiedPlaces(inValid));
	}
	
	@Test
	public void connectedTest() {
		Stone[] valid = new Stone[] {new Stone('x', 'b', 1, 1), new Stone('x', 'b', 2, 1), new Stone('x', 'b', 3, 1)};
		Stone[] inValid = new Stone[] {new Stone('x', 'b', 0, 0), new Stone('x', 'b', 0, 2), new Stone('x', 'b', 1, 2)};
		//TestTUI tui = new TestTUI(board);
		//tui.drawBoard();
		assertTrue(board.areConnected(valid));
		assertFalse(board.areConnected(inValid));
	}
	
	@Test
	public void validMoveTest() {
		board.resetMap();
		Stone[] valid = new Stone[] {new Stone('x', 'b', -1, 1), new Stone('x', 'b', 0, 1), new Stone('x', 'b', 1, 1)};
		Stone[] valid2 = new Stone[] {new Stone('x', 'b', 1, 0), new Stone('x', 'b', 2, 0), new Stone('x', 'b', 3, 0)};
		Stone[] inValid = new Stone[] {new Stone('x', 'b', 0, 0), new Stone('x', 'b', 0, 1), new Stone('x', 'b', 1, 0)};
		Stone[] inValid2 = new Stone[] {new Stone('x', 'b', 0, 0), new Stone('x', 'b', 1, 1), new Stone('x', 'b', 2, 2)};
		
		assertTrue(board.isValidMove(valid));
		assertTrue(board.isValidMove(valid2));
		assertFalse(board.isValidMove(inValid));
		assertFalse(board.isValidMove(inValid2));
	}
	
	@Test
	public void shapeColorTest() {
		Stone[] valid = new Stone[] {new Stone('x', 'b', -1, 1), new Stone('x', 'd', 0, 1), new Stone('x', 'c', 1, 1)};
		Stone[] valid2 = new Stone[] {new Stone('x', 'b', 1, 0), new Stone('e', 'b', 2, 0), new Stone('r', 'b', 3, 0)};
		Stone[] inValid = new Stone[] {new Stone('x', 'b', 0, 0), new Stone('x', 'b', 0, 1), new Stone('x', 'b', 1, 0)};
		Stone[] inValid2 = new Stone[] {new Stone('x', 'b', 0, 0), new Stone('x', 'c', 1, 1), new Stone('r', 'c', 2, 2)};
		
		assertTrue(board.validShapeColorCombination(valid));
		assertTrue(board.validShapeColorCombination(valid2));
		assertFalse(board.validShapeColorCombination(inValid));
		assertFalse(board.validShapeColorCombination(inValid2));
	}
}
