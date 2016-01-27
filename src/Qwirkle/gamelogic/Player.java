package qwirkle.gamelogic;

import qwirkle.client.Client;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private Board board;
    private int score;
    private int lastScoreUpdate = 0;
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

    public boolean hasTurn() {
        return this.hasTurn;
    }

    public Board getBoard() {
        return this.board;
    }
    
    public int getScore() {
    	return score;
    }

    public void undoLastMove() {
        this.board.undoMove();
        this.resetHand();
        this.score = this.score - this.lastScoreUpdate;
        this.lastScoreUpdate = 0;
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
        } else {
        	this.score = this.score + score;
        	this.lastScoreUpdate = score;
        }
        return score;
    }
    
    /** 
     * Makes the moves on board the server has sent back, opponents made these moves.
     * Main difference is that this doesn't add score.
     * @param stones
     */
    public int updateBoard(Stone[] stones) {
    	int updated = board.makeMove(stones);
    	board.removeLastMoves();
    	return updated;
    }

    //only if board is empty.
    public Stone[] tradeStones(Stone[] stones) {
        return null;
    }

    public ArrayList<Stone> getHand() {
        return hand;
    }
}
