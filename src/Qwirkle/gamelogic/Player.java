package qwirkle.gamelogic;

import qwirkle.client.Client;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private Board board;
    public Client client;
    private ArrayList<Stone> hand = new ArrayList<>(); //stones that are in the hand.
    private boolean hasTurn;
    private ArrayList<Stone> handBackup;

    public Player(Client client) {
        this.client = client;
        this.client.setPlayer(this);
        this.name = client.getName();
        this.board = new Board();
    }

    public void addStoneToHand(Stone stone) {
        hand.add(stone);
    }

    // TODO implement has Turn functionality
    public boolean hasTurn() {
        return this.hasTurn;
    }

    public void setTurn() {
        //todo
    }

    public Board getBoard() {
        return this.board;
    }

    public void undoLastMove() {
        this.board.undoMove();
        this.resetHand();
    }

    public void resetHand() {
        if (this.handBackup.size() > 0) {
            this.hand = new ArrayList<Stone>(this.handBackup);
        }
    }

    public void saveHand() {
        this.handBackup = new ArrayList<Stone>(this.hand);
    }

    public String getName() {
        return this.name;
    }

    public int makeMove(Stone[] stones) {

        int score = -1;
        score = board.makeMove(stones);

        // If invalid move, undo last move
        if (score == -1) {
            undoLastMove();
        }
        return score;
    }

    //only if board is empty.
    public Stone[] tradeStones(Stone[] stones) {
        return null;
    }

    public void skipTurn() {

    }

    /*
     * Gets the longest line possible with the stones held in the hand.
     * TODO: not needed thus to large to implement now.
     */
    public int getLongestCombination() {
        int longest = 0;
        Stone startingStone;
        List<Stone> foundShapes = new ArrayList<Stone>();
        List<Stone> foundColors = new ArrayList<Stone>();

        for (int i = 0; i < hand.size(); i++) {
            startingStone = hand.get(i);
            for (int j = 0; j < hand.size(); j++) {
                if (j != i) {
                    if (startingStone.getColor() == hand.get(j).getColor()
                        && startingStone.getShape() != hand.get(j).getShape()) {
                        foundColors.add(hand.get(j));
                    }
                    if (startingStone.getColor() != hand.get(j).getColor()
                        && startingStone.getShape() == hand.get(j).getShape()) {
                        foundShapes.add(hand.get(j));
                    }
                }
            }
        }
        return 0;
    }

    public ArrayList<Stone> getHand() {
        return hand;
    }
}
