package Qwirkle.tests;

import java.io.PrintWriter;

import Qwirkle.gamelogic.Board;
import Qwirkle.gamelogic.Coordinate;
import Qwirkle.gamelogic.Stone;

public class TestTUI {
    PrintWriter out;

    private Board board;

    public TestTUI(Board board) {
        this.board = board;
    }

    public static void main(String[] args) {
        TestTUI tui = new TestTUI(new Board());
        tui.board.createTestMap();
        //tui.drawBoard();
    }
}
