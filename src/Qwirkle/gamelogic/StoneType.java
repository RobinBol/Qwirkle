package qwirkle.gamelogic;

public class StoneType {
	private char color;
	private char shape;
	
	public StoneType(char c, char s) {
		this.color = c;
		this.shape = s;
	}
	
	public char getColor() {
		return this.color;
	}
	
	public char getShape() {
		return this.shape;
	}
	
	@Override
	public int hashCode() {
		return ("" + color + shape).hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		return o.hashCode() == this.hashCode();
	}
	
	public String toString() {
		return "" + color + shape + hashCode();
	}
}
