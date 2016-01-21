package qwirkle.gamelogic;

import qwirkle.client.Client;
import qwirkle.util.Input;
import qwirkle.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private Board board;
    public Client client;
    private Stone[] hand; //stones that are in the hand.
    private boolean hasTurn;

    public Player(Client client) {
        this.client = client;
        this.client.setPlayer(this);
        this.name = client.getName();
        this.board = new Board();
    }

    // TODO implement has Turn functionality
    public boolean hasTurn(){
        return this.hasTurn;
    }
    public void setTurn(){
        //todo
    }
    public void undoLastMove() {
        this.board.undoMove();
    }

    public String getName() {
        return this.name;
    }

    //boolean in case makeMove didn't execute well.
    public boolean makeMove() {
// TODO fix this function
//        Logger.print("DIT ZIJN JE stenen");
//        String stones = Input.ask("make a move", client);
//        // valideer/convert stenen
//        // doe lokaal die move
//        Stone[] stones =
//        board.makeMove(stones);
//
//        client.sendMessage(VALIDEERMOVE);
        return false;
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

        for (int i = 0; i < hand.length; i++) {
            startingStone = hand[i];
            for (int j = 0; j < hand.length; j++) {
                if (j != i) {
                    if (startingStone.getColor() == hand[j].getColor() && startingStone.getShape() != hand[j].getShape()) {
                        foundColors.add(hand[j]);
                    }
                    if (startingStone.getColor() != hand[j].getColor() && startingStone.getShape() == hand[j].getShape()) {
                        foundShapes.add(hand[j]);
                    }
                }
            }
        }
        return 0;
    }

    public Stone[] getHand() {
        return hand;
    }
}
