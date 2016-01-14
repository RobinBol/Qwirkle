package QwirkleUI;

import GameLogic.Board;
import GameLogic.Coordinate;
import GameLogic.Stone;

public class TUIBasic {
private Board board;
	
	public TUIBasic(Board board) {
		this.board = board;
	}
	
	public void drawBoard() {
		int[] boardSize = board.getBoardWidthHeight();
		int maxSize = Math.max(boardSize[0], boardSize[1]);
		int middle = (int) (maxSize / 2) + 4;
		for (int i = 0 - middle; i < maxSize + 11 - middle; i++) {
			System.out.print("|");
			for (int j = 0 - middle; j < maxSize + 11 - middle; j++) {
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
}
