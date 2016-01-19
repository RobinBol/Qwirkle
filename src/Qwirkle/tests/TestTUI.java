package qwirkle.tests;

import qwirkle.gamelogic.Board;

public class TestTUI {

    private Board board;

    public TestTUI(Board board) {
        this.board = board;
    }

    public static void main(String[] args) {
        Board board = new Board();
        TestTUI tui = new TestTUI(board);
        tui.board.createTestMap();
        System.out.println(board);
    }
}
