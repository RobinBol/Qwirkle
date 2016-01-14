package Tests;

import GameLogic.Board;
import GameLogic.Player;

public class PlayerTest {

	Player player;
	
	public static void main(String[] args) {
		Player player = new Player(new Board());
		for (int i = 0; i < player.getHand().length; i++) {
			System.out.println(player.getHand()[i]);
		}
		
	}

}
