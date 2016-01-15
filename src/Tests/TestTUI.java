package Tests;

import java.io.PrintWriter;

import GameLogic.Board;
import GameLogic.Coordinate;
import GameLogic.Stone;

public class TestTUI {
    PrintWriter out;

    private Board board;

    public TestTUI(Board board) {
        this.board = board;
    }

    public void drawBoard() {

        int middle = (int) Board.MAXBOARDSIZE / 2;
        for (int i = 0 - middle; i < Board.MAXBOARDSIZE - middle; i++) {
            System.out.print("|");
            for (int j = 0 - middle; j < Board.MAXBOARDSIZE - middle; j++) {
                Stone stone = board.getBoard().get(Coordinate.getCoordinateHash(j, i));
                if (stone != null) {
                    System.out.print(stone.getShape() + "" + stone.getColor());
                } else {
                    System.out.print("NN");
                }
                System.out.print("|");
            }
            System.out.println();
        }

    }

    public static void main(String[] args) {
        TestTUI tui = new TestTUI(new Board());
        tui.board.createTestMap();
        tui.drawBoard();
    }
}
