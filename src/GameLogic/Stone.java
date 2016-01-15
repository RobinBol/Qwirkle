package gamelogic;

public class Stone {

    //maybe unique id?
    private int id;

    private char shape, color;
    private int x, y;

    public Stone up, down, left, right;

    //Round, X, Diamond, Square, Star, Plus.
    public static final char[] SHAPES = {'r', 'x', 'd', 's', '*', '+'};
    //Red, Orange, Yellow, Green, Blue, Purple.
    public static final char[] COLORS = {'r', 'o', 'y', 'g', 'b', 'p'};

    public Stone(char shape, char color, int x, int y) {
        this.shape = shape;
        this.color = color;
        this.x = x;
        this.y = y;
        //Maybe save the coordinates?
    }

    //might be unfair distribution due to how type casting works to int.
    public static char getRandomShape() {
        return SHAPES[(int) (Math.random() * SHAPES.length)];
    }

    public static char getRandomColor() {
        return COLORS[(int) (Math.random() * COLORS.length)];
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

    public boolean isValidStone() {
        if (allowedShape(getShape()) && allowedColor(getColor())) return true;
        return false;
    }

    public static boolean allowedShape(char c) {
        for (int i = 0; i < SHAPES.length; i++) {
            if (c == SHAPES[i]) return true;
        }
        return false;
    }

    public static boolean allowedColor(char c) {
        for (int i = 0; i < COLORS.length; i++) {
            if (c == COLORS[i]) return true;
        }
        return false;
    }

    public String toString() {
        return shape + "" + color;
    }

}
