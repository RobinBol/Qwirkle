package GameLogic;

import java.util.Scanner;

public final class Coordinate {

    public Coordinate(int x, int y){

    }

    //Override
    public void getHashCode (){};

    //Override
    public void equals(){};
    
    /*
     * Returns an String to pas into the hashmap the contains the keys to tiles that may or may not contain a stone.
     * The format is N:123:P:353 N mean its negative P means its positive.
     * Usage hashmap.get(GameLogic.Coordinate.getCoordinateHash(x, y)).
     * Will return the value<GameLogic.Stone>.
     * .
     * @param x The x coordinate of the stone you want to request.
     * @param y The y coordinate of the stone you want to request.
     * 
     * 
     */
    public static String getCoordinateHash(int x, int y) {
    	String string = "";
    	if (x < 0) {
    		string = string + "N:";
    	} else {
    		string = string + "P:";
    	}
    	string = string + Math.abs(x) + ":";
    	if (y < 0) {
    		string = string + "N:";
    	} else {
    		string = string + "P:";
    	}
    	string = string + Math.abs(y);
    	return string;
    }
    
    public static int[] getCoordinates(String hashedCoordinate) {
    	String[] string = hashedCoordinate.split(":");
    	int x, y;
    	if (string[0].equals("N")) {
    		x = Integer.parseInt(string[1]) * -1;
    	} else {
    		x = Integer.parseInt(string[1]);
    	}
    	if (string[2].equals("N")) {
    		y = Integer.parseInt(string[3]) * -1;
    	} else {
    		y = Integer.parseInt(string[3]);
    	}
    	return new int[] {x, y};
    }
}
