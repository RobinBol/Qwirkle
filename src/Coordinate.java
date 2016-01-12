public final class Coordinate {

    public Coordinate(int x, int y){

    }

    //Override
    public void getHashCode (){};

    //Override
    public void equals(){};
    
    /*
     * Returns an integer to pas into the hashmap the contains the keys to tiles that may or may not contain a stone.
     * Usage hashmap.get(Coordinate.getCoordinateHash(x, y));
     * Will return the value<Stone>; 
     */
    public static int getCoordinateHash(int x, int y) {
    	return x * 10000 + y;
    }
    
}
