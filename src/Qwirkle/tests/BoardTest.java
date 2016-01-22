package qwirkle.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import qwirkle.gamelogic.Board;
import qwirkle.gamelogic.Stone;

public class BoardTest {

    Board board;

    @Before
    public void setUp() throws Exception {
        board = new Board();
        board.createTestMap();
    }

    @Test
    public void sameRowTest() {
        Stone[] valid = new Stone[]{new Stone('x', 'b', 0, 1), new Stone('x', 'b', 0, 2), new Stone('x', 'b', 0, 7)};
        Stone[] valid2 = new Stone[]{new Stone('x', 'b', 0, 0), new Stone('x', 'b', 1, 0), new Stone('x', 'b', 2, 0)};
        Stone[] inValid = new Stone[]{new Stone('x', 'b', 0, 1), new Stone('x', 'b', 0, 2), new Stone('x', 'b', 1, 2)};
        Stone[] inValid2 = new Stone[]{new Stone('x', 'b', 0, 1), new Stone('x', 'b', 0, 2), new Stone('x', 'b', 1, 7)};

        assertTrue(board.inSameRow(valid));
        assertTrue(board.inSameRow(valid2));
        assertFalse(board.inSameRow(inValid));
        assertFalse(board.inSameRow(inValid2));
    }

    @Test
    public void occupiedPlacesTest() {
        board.createTestMap();
        Stone[] valid = new Stone[]{new Stone('x', 'b', 1, 1), new Stone('x', 'b', 2, 2), new Stone('x', 'b', 3, 3)};
        Stone[] inValid = new Stone[]{new Stone('x', 'b', 0, 0), new Stone('x', 'b', 0, 2), new Stone('x', 'b', 1, 2)};
        //TestTUI tui = new TestTUI(board);
        //tui.drawBoard();
        assertFalse(board.takeOccupiedPlaces(valid));
        assertTrue(board.takeOccupiedPlaces(inValid));
    }

    @Test
    public void connectedTest() {
        Stone[] valid = new Stone[]{new Stone('x', 'b', 1, 1), new Stone('x', 'b', 2, 1), new Stone('x', 'b', 3, 1)};
        Stone[] inValid = new Stone[]{new Stone('x', 'b', 0, 0), new Stone('x', 'b', 0, 2), new Stone('x', 'b', 1, 2)};
        //TestTUI tui = new TestTUI(board);
        //tui.drawBoard();
        assertTrue(board.areConnected(valid));
        assertFalse(board.areConnected(inValid));
    }

    @Test
    public void validMoveTest() {
        board.resetMap();
        board.createTestMap();
        System.out.println(board.getBoard().keySet());
        Stone[] valid = new Stone[]{new Stone('A', 'A', -1, 1), new Stone('B', 'A', 0, 1), new Stone('C', 'A', 1, 1)};
        Stone[] valid2 = new Stone[]{new Stone('A', 'A', 1, 1), new Stone('B', 'A', 2, 1), new Stone('C', 'A', 3, 1)};
        Stone[] inValid = new Stone[]{new Stone('x', 'b', 0, 0), new Stone('x', 'b', 0, 1), new Stone('x', 'b', 1, 0)};
        Stone[] inValid2 = new Stone[]{new Stone('x', 'b', 0, 0), new Stone('x', 'b', 1, 1), new Stone('x', 'b', 2, 2)};

        assertTrue(board.isValidMove(valid));
        assertTrue(board.isValidMove(valid2));
        assertFalse(board.isValidMove(inValid));
        assertFalse(board.isValidMove(inValid2));
    }

    @Test
    public void makeMoveTest() {
        board.resetMap();
        board.createTestMap();
        System.out.println(board.getBoard().keySet());
        Stone[] valid = new Stone[]{new Stone('B', 'A', -1, -1), new Stone('C', 'A', 0, -1), new Stone('D', 'A', 1, -1)};
        Stone[] valid2 = new Stone[]{new Stone('B', 'C', -1, -2)};
        Stone[] valid3 = new Stone[]{
                new Stone('B', 'D', -1, -3),
                new Stone('B', 'A', 0, -3),
                new Stone('B', 'B', 1, -3),
                new Stone('B', 'C', 2, -3),
                new Stone('B', 'E', 3, -3),
                new Stone('B', 'F', 4, -3)};
        Stone[] inValid = new Stone[]{new Stone('B', 'G', 5, -3)};

        assertEquals(9, board.makeMove(valid));
        assertEquals(3, board.makeMove(valid2));
        assertEquals(16, board.makeMove(valid3));
        assertEquals(-1, board.makeMove(inValid));
        System.out.println(board);

        board.resetMap();
        assertEquals(1, board.makeMove(new Stone[]{new Stone('A', 'B', 0, 0)}));
        System.out.println(board);
    }

    @Test
    public void shapeColorTest() {
        Stone[] valid = new Stone[]{new Stone('x', 'b', -1, 1), new Stone('x', 'o', 0, 1), new Stone('x', 'y', 1, 1)};
        Stone[] valid2 = new Stone[]{new Stone('x', 'b', 1, 0), new Stone('+', 'b', 2, 0), new Stone('*', 'b', 3, 0)};
        Stone[] inValid = new Stone[]{new Stone('x', 'b', 0, 0), new Stone('x', 'b', 0, 1), new Stone('x', 'b', 1, 0)};
        Stone[] inValid2 = new Stone[]{new Stone('x', 'b', 0, 0), new Stone('x', 'c', 1, 1), new Stone('r', 'c', 2, 2)};

        assertTrue(board.validShapeColorCombination(valid));
        assertTrue(board.validShapeColorCombination(valid2));
        assertFalse(board.validShapeColorCombination(inValid));
        assertFalse(board.validShapeColorCombination(inValid2));
    }

    @Test
    public void connectionTest() {
        board.resetMap();
        board.createTestMap();
        Stone valid = new Stone('a', 'a', 0, -1);
        Stone valid2 = new Stone('a', 'a', 0, 1);
        Stone inValid = new Stone('a', 'a', 0, -2);
        Stone inValid2 = new Stone('a', 'a', -4, 0);

        assertTrue(board.isConnected(valid));
        assertTrue(board.isConnected(valid2));
        assertFalse(board.isConnected(inValid));
        assertFalse(board.isConnected(inValid2));
    }
}
