package qwirkle.gamelogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bag {
    public static final int AMOUNTOFEACH = 3;
    private List<Stone> stones;

    public Bag() {
        initializeBag();
    }

    public void initializeBag() {
        stones = new ArrayList<>();
        //TODO: generate stones for bag;
        for (int i = 0; i < Stone.SHAPES.length; i++) {
            for (int j = 0; j < Stone.COLORS.length; j++) {
                for (int j2 = 0; j2 < AMOUNTOFEACH; j2++) {
                    stones.add(new Stone(Stone.SHAPES[i], Stone.COLORS[j], -1, -1));
                }
            }
        }
    }

    /**
     * Trade stones with the bag
     * TODO: Make sure the bag is not empty and so on.
     */

    public Stone[] tradeStones(Stone[] stones) {
        Stone[] newStones = new Stone[stones.length];
        for (int i = 0; i < stones.length; i++) {
            addStone(stones[i]);
            newStones[i] = takeStone();
        }
        return newStones;
    }

    /**
     * Add stones back to the bag.
     */
    public void addStone(Stone s) {
        stones.add(s);
    }

    /**
     * Takes a random stone from the bag.
     */
    public Stone takeStone() {
        Random random = new Random();
        if (stones.size() > 0) {
            return stones.get(random.nextInt(stones.size() - 1));
        } else {
            return null;
        }
    }

    public String toString() {
        String string = "";
        for (int i = 0; i < stones.size(); i++) {
            Stone stone = stones.get(i);
            string = string + stone.getShape() + stone.getColor() + ":";
        }
        return string;
    }
}
