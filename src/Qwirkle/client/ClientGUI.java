package qwirkle.client;

import java.awt.Graphics;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;

import qwirkle.gamelogic.Board;
import qwirkle.gamelogic.Stone;

public class ClientGUI extends JPanel {

    private Board board;
    public JFrame jFrame;

    public static final int TILESIZE = 30;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final int TILESPACING = 2;

    private int xOffset;
    private int yOffset;

    public ClientGUI(Board board) {

        this.board = board;

        //tests
        board.createTestMap();
        System.out.println(board);
        Stone[] valid = new Stone[]{new Stone('B', 'A', -1, -1), new Stone('C', 'A', 0, -1), new Stone('D', 'A', 1, -1)};
        board.makeMove(valid);

    }

    public void paint(Graphics g) {
        Map<String, Stone> boardMap = board.getBoard();
        int[] mapMiddle = board.getBoardWidthHeight();
        this.xOffset = (WIDTH / 2);
        this.yOffset = (HEIGHT / 2);
        int iteration = 0;
        for (Stone stone : boardMap.values()) {
            //g.fillRect(stone.getX() * (TILESIZE + TILESPACING) + xOffset, stone.getY() * (TILESIZE + TILESPACING) + yOffset, TILESIZE, TILESIZE);
            drawPlus(g, stone.getX(), stone.getY());
            iteration++;
        }
        g.drawLine(0, 300, 100, 300);


    }

    public void drawPlus(Graphics g, int x, int y) {
        g.fillRect(x * (TILESIZE + TILESPACING) + xOffset + ((TILESIZE / 2) - (TILESIZE / 6)), y * (TILESIZE + TILESPACING) + yOffset, TILESIZE / 3, TILESIZE);
        g.fillRect(x * (TILESIZE + TILESPACING) + xOffset, y * (TILESIZE + TILESPACING) + yOffset + ((TILESIZE / 2) - (TILESIZE / 6)), TILESIZE, TILESIZE / 3);
    }

    public static void main(String[] args) {
        JFrame jFrame = new JFrame("Awesomest Qwirkle Game");
        jFrame.setSize(WIDTH, HEIGHT);
        jFrame.add(new ClientGUI(new Board()));
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
