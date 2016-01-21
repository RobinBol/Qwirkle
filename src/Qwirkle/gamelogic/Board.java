package qwirkle.gamelogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {
    //public static final int MAXBOARDSIZE = 11;

    private Bag bag;
    private Map<String, Stone> board;
    private List<Stone> lastMoves;


    public Board() {
        board = new HashMap<>();
//        bag = new Bag();
        lastMoves = new ArrayList<>();
    }

    /**
     * Places a stone on the board.
     * Stones are mostly validated before this call.
     * The stone is connected top top down left right stones if they are available.
     *
     * @param stone takes a Stone and make sure it is placed properly on the board.
     */
    public void placeStone(Stone stone) {
        int x = stone.getX();
        int y = stone.getY();
        stone.down = board.get(Coordinate.getCoordinateHash(x, y - 1));
        if (stone.down != null) stone.down.up = stone;

        stone.left = board.get(Coordinate.getCoordinateHash(x - 1, y));
        if (stone.left != null) stone.left.right = stone;

        stone.right = board.get(Coordinate.getCoordinateHash(x + 1, y));
        if (stone.right != null) stone.right.left = stone;

        stone.up = board.get(Coordinate.getCoordinateHash(x, y + 1));
        if (stone.up != null) stone.up.down = stone;

        board.put(Coordinate.getCoordinateHash(stone.getX(), stone.getY()), stone);
    }

    public void removeStone(Stone stone) {
        int x = stone.getX();
        int y = stone.getY();
        stone.down = board.get(Coordinate.getCoordinateHash(x, y - 1));
        if (stone.down != null) stone.down.up = null;

        stone.left = board.get(Coordinate.getCoordinateHash(x - 1, y));
        if (stone.left != null) stone.left.right = null;

        stone.right = board.get(Coordinate.getCoordinateHash(x + 1, y));
        if (stone.right != null) stone.right.left = null;

        stone.up = board.get(Coordinate.getCoordinateHash(x, y + 1));
        if (stone.up != null) stone.up.down = null;
        board.remove(Coordinate.getCoordinateHash(stone.getX(), stone.getY()));
    }


    public Stone getStone(int x, int y) {
        return board.get(Coordinate.getCoordinateHash(x, y));
    }

    public Map<String, Stone> getBoard() {
        return board;
    }

    public void undoMove() {
        for (int i = 0; i < lastMoves.size(); i++) {
            removeStone(lastMoves.get(i));
        }
    }

    /* Makes a move.
     * validates if the move is possible.
     * then places a stone at given position.
     */
    public boolean makeMove(int x, int y, Stone stone) {
        if (makeMove(x, y, stone.getShape(), stone.getColor())) {
            return true;
        }
        return false;
    }

    public boolean makeMove(Stone[] stones) {
        if (isValidMove(stones)) {
            boolean searchY = false;
            int sameX = 0;
            int startX = stones[0].getX();
            for (int i = 0; i < stones.length; i++) {
                placeStone(stones[i]);
                if (stones[i].getX() == startX) {
                    sameX++;
                }
            }
            searchY = sameX != stones.length;
            System.out.println(searchY + " " + sameX);

            //after stones are placed go calculate scores and check valid placement.
            List<Stone> moves = Arrays.asList(stones);
            List<Stone[]> allRows = new ArrayList<>();
            List<Stone> checkRow = new ArrayList<>();

            Stone current;
            for (int i = 0; i < stones.length; i++) {
                current = moves.get(i);
                checkRow = getRows(searchY, current);
                if (checkRow.size() > 0) {
                    Stone[] array = checkRow.toArray(new Stone[checkRow.size()]);
                    allRows.add(array);
                }
                checkRow.clear();
            }
            checkRow = getRows(!searchY, stones[0]);
            System.out.println(checkRow.toString());
            Stone[] array = checkRow.toArray(new Stone[checkRow.size()]);
            checkRow.clear();
            allRows.add(array);


            //check validity moves.
            int score = 0;
            for (int i = 0; i < allRows.size(); i++) {
                System.out.println(Arrays.toString(allRows.get(i)));
                if (isValidPlacedMove(allRows.get(i))) {
                    System.out.println(score);
                    score = score + allRows.get(i).length;
                    if (allRows.get(i).length == 6) {
                        score = score + 6;
                    }
                }
            }
            System.out.println(score);

            //save last made moves.
            lastMoves = moves;
        }


        return true;
    }

    public List<Stone> getRows(boolean searchDirection, Stone current) {
        boolean done = false;
        List<Stone> checkRow = new ArrayList<>();
        checkRow.add(current);
        if (searchDirection) { //searchY
            while (!done) {

                while (current.up != null) {
                    if (!checkRow.contains(current.up)) {
                        checkRow.add(current.up);
                    }
                    current = current.up;
                }
                while (current.down != null) {
                    if (!checkRow.contains(current.down)) {
                        checkRow.add(current.down);
                    }
                    current = current.down;

                }
                System.out.println(checkRow.toString() + "UPDOWN");
                done = true;
            }
        } else {
            while (!done) {
                while (current.left != null) {
                    if (!checkRow.contains(current.left)) {
                        checkRow.add(current.left);
                    }
                    current = current.left;
                }
                while (current.right != null) {
                    if (!checkRow.contains(current.right)) {
                        checkRow.add(current.right);
                    }
                    current = current.right;
                }
                System.out.println(checkRow.toString() + "LEFTRIGHT");
                done = true;
            }
        }
        return checkRow;
    }

    public boolean makeMove(int x, int y, char shape, char color) {
        //if it is the first move on the board.
        if (isEmptyBoard()) {
            if (x == 0 && y == 0) {
                //TODO: Not sure if the new stone might create duplicate cases. Make sure this is addressed in player.
                //placeStone(x, y, new Stone(shape, color, x, y));
            }
            //not the first tile on 0,0;
            //TODO: Announce to player that it is an invalid move for the first turn?
            //Or just do this in the player object.

            //System.out.println("Invalid first move");
            return false;
        }
        return false;
    }


    public boolean isEmptyBoard() {
        return board.isEmpty();
    }

    public boolean isEmptySpot(int x, int y) {
        if (getStone(x, y) == null) {
            return true;
        }
        return false;
    }

    public boolean isValidMove(Stone[] stones) {
        if (!areValidStones(stones)) return false;
        if (!inSameRow(stones)) return false;
        if (!areConnected(stones)) return false;
        if (takeOccupiedPlaces(stones)) return false;
        if (!validShapeColorCombination(stones)) return false;
        if (!isEmptyBoard()) {
            if (!areConnectedToBoard(stones)) return false;
        } else {
            if (!containsZeroZero(stones)) return false;
        }

        return true;
    }

    public boolean isValidPlacedMove(Stone[] stones) {
        if (!areValidStones(stones)) return false;
        if (!inSameRow(stones)) return false;
        if (!areConnected(stones)) return false;
        if (!validShapeColorCombination(stones)) return false;

        return true;
    }

    /*
     * Tests if the stones in array (moves) are in connected to each other.
	 */
    public boolean areConnected(Stone[] stones) {
        if (!inSameRow(stones)) return false;
        int lowestX = Integer.MAX_VALUE;
        int lowestY = Integer.MAX_VALUE;
        int highestX = Integer.MIN_VALUE;
        int highestY = Integer.MIN_VALUE;
        Stone first = stones[0];
        for (int i = 0; i < stones.length; i++) {
            if (first.getY() == stones[i].getY()) {
                lowestX = Math.min(lowestX, stones[i].getX());
                highestX = Math.max(highestX, stones[i].getX());
            }
            if (first.getX() == stones[i].getX()) {
                lowestY = Math.min(lowestY, stones[i].getY());
                highestY = Math.max(highestY, stones[i].getY());
            }
        }
        //System.out.println(stones.length);
        //System.out.println(lowestX);
        return ((highestX - lowestX) + 1 == stones.length || (highestY - lowestY) + 1 == stones.length);
    }

    public boolean inSameRow(Stone[] stones) {
        int amountSameX = 0;
        int amountSameY = 0;
        Stone first = stones[0];
        for (Stone stone : stones) {
            if (first.getX() == stone.getX()) {
                amountSameX++;
            }
            if (first.getY() == stone.getY()) {
                amountSameY++;
            }
        }
        return (amountSameX == stones.length || amountSameY == stones.length);
    }

    public boolean validShapeColorCombination(Stone[] stones) {
        int amountSameShape = 0;
        int amountSameColor = 0;
        Stone first = stones[0];
        for (Stone stone : stones) {
            if (first.getShape() == stone.getShape()) {
                amountSameShape++;
            }
            if (first.getColor() == stone.getColor()) {
                amountSameColor++;
            }
        }
        return (amountSameShape == stones.length && amountSameColor == 1) || (amountSameColor == stones.length && amountSameShape == 1);
    }

    public boolean areValidStones(Stone[] stones) {
        for (int i = 0; i < stones.length; i++) {
            if (!stones[i].isValidStone()) return false;
        }
        return true;
    }

    /*
     * returns if a position is next to an already placed stone.
     */
    public boolean isConnected(Stone stone) {
        int x = stone.getX();
        int y = stone.getY();
        if (board.containsKey(Coordinate.getCoordinateHash(x + 1, y))) return true;
        if (board.containsKey(Coordinate.getCoordinateHash(x, y + 1))) return true;
        if (board.containsKey(Coordinate.getCoordinateHash(x - 1, y))) return true;
        if (board.containsKey(Coordinate.getCoordinateHash(x, y - 1))) return true;
        return false;
    }

    public boolean areConnectedToBoard(Stone[] stones) {
        System.out.println(board.keySet() + ";;");
        for (int i = 0; i < stones.length; i++) {
            if (isConnected(stones[i])) {
                System.out.println(isConnected(stones[i]));
                return true;
            }
        }
        return false;
    }

    public boolean containsZeroZero(Stone[] stones) {
        for (int i = 0; i < stones.length; i++) {
            if (stones[i].getX() == 0 && stones[i].getY() == 0) return true;
        }
        return false;
    }

    public boolean takeOccupiedPlaces(Stone[] stones) {
        for (Stone stone : stones) {
            String positionHash = Coordinate.getCoordinateHash(stone.getX(), stone.getY());
            if (board.containsKey(positionHash)) {
                return true;
            }
        }
        return false;
    }

    public void createTestMap() {
        for (int i = -2; i < 4; i++) {
            //test purpose only, is not valid according to game rules.
            board.put(Coordinate.getCoordinateHash(i, 0), new Stone(Stone.SHAPES[i + 2], Stone.COLORS[i + 2], i, 0));
        }
        //board.put(Coordinate.getCoordinateHash(-2, -1), new Stone(Stone.SHAPES[0], Stone.COLORS[4], -2, -1));
    }

    public void resetMap() {
        board.clear();
    }

    public int[] getBoardWidthHeight() {
        if (isEmptyBoard()) return new int[]{0, 0};
        int lowestX = 0;
        int lowestY = 0;
        int highestX = 0;
        int highestY = 0;
        int[] testCoord;
        for (String coordinate : board.keySet()) {
            testCoord = Coordinate.getCoordinates(coordinate);
            if (testCoord[0] < lowestX) lowestX = testCoord[0];
            if (testCoord[1] < lowestY) lowestY = testCoord[1];
            if (testCoord[0] > highestX) highestX = testCoord[0];
            if (testCoord[1] > highestY) highestY = testCoord[1];
        }
        int width = highestX - lowestX + 1;
        int height = highestY - lowestY + 1;

        return new int[]{width, height};
    }

    @Override
    public String toString() {

        String boardString = "";
        // Calculate boardSize
        int[] boardSize = this.getBoardWidthHeight();
        int maxSize = Math.max(boardSize[0], boardSize[1]);
        int middle = (maxSize / 2) + 4;

        // For loop that will loop over the stones on the board and print them
        for (int i = 0 - middle; i < maxSize + 11 - middle; i++) {
            boardString = boardString + String.format("%3s", ""+ i) + "|";
            for (int j = 0 - middle; j < maxSize + 11 - middle; j++) {
            	if ( j == 0 ) { System.out.println(String.format("%3s", ""+ j));}
                Stone stone = this.getBoard().get(Coordinate.getCoordinateHash(j, i));
                if (stone != null) {
                    boardString = boardString + stone.getShape() + "" + stone.getColor();
                } else {
                    boardString = boardString + "NN";
                }
                boardString = boardString + "|";
            }
            boardString = boardString + "\n";
        }

        return boardString;
    }
}
