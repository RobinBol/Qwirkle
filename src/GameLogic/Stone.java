package GameLogic;

import java.lang.reflect.GenericArrayType;

public class Stone {
	
	//maybe unique id?
	private int id;
	
	private char shape, color;
	private int x, y;
	
	public Stone up, down, left, right;	
	
	//Round, X, Diamond, Square, Star, Plus.
	public static final char[] shapes = {'r','x','d','s','*','+'};
	//Red, Orange, Yellow, Green, Blue, Purple.
	public static final char[] colors = {'r','o','y','g','b','p'};
	
	public Stone(char shape, char color, int x, int y) {
		this.shape = shape;
		this.color = color;
		this.x = x;
		this.y = y;
		//Maybe save the coordinates?
	}
	
	//might be unfair distribution due to how type casting works to int.
	public static char getRandomShape() {
		return shapes[(int)(Math.random() * shapes.length)];
	}
	
	public static char getRandomColor() {
		return colors[(int)(Math.random() * colors.length)];
	}
	
	public char getShape() {
		return this.shape;
	}
	
	public char getColor() {
		return this.color;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public String toString() {
		return shape + "" + color;
	}

}
