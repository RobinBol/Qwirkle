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
	
	public static final int TILESIZE = 3;
	
	public ClientGUI(Board board) {
		jFrame = new JFrame("Awesomest Qwirkle Game");
		jFrame.setSize(800, 600);
		this.board = board;
		
		//tests
		board.createTestMap();
	}
	
	public void paint(Graphics g) {
		Map<String, Stone> boardMap = board.getBoard();
		int[] mapMiddle = board.getBoardWidthHeight();
		int iteration = 0;
		for (Stone stone : boardMap.values()) {
			g.fillRect(stone.getX() + iteration, stone.getY() + iteration, TILESIZE, TILESIZE);
			iteration++;
		}
		
	}
	
	public static void main(String[] args) {
		ClientGUI gui = new ClientGUI(new Board());
		gui.jFrame.setVisible(true);
	}
}
