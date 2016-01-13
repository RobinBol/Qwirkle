package GameLogic;

public class Stone {
	
	//maybe unique id?
	private int id;
	
	private char shape, color;
	
	//Round, X, Diamond, Square, Star, Plus.
	public static final char[] shapes = {'r','x','d','s','*','+'};
	//Red, Orange, Yellow, Green, Blue, Purple.
	public static final char[] colors = {'r','o','y','g','b','p'};
	
	public Stone(char shape, char color) {
		this.shape = shape;
		this.color = color;
	}
	
	//might be unfair distribution due to how type casting works to int.
	public static char getRandomShape() {
		return shapes[(int)(Math.random() * (shapes.length + 0.99999))];
	}
	
	public static char getRandomColor() {
		return colors[(int)(Math.random() * (colors.length + 0.99999))];
	}

}
