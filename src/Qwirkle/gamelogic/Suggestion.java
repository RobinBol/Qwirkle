package qwirkle.gamelogic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Suggestion {
    int x, y;
    int score = 0;
    List<StoneType> possiblePlacements;

    public Suggestion(int x, int y) {
        possiblePlacements = new ArrayList<>();
        this.x = x;
        this.y = y;
    }

    public int getScore() {
        return score;
    }

    public void addType(List<StoneType> types, int score) {
        if (possiblePlacements.isEmpty()) {
            for (StoneType type : types) {
                possiblePlacements.add(type);
            }
            this.score = this.score + score;
        } else {
            List<StoneType> iterationList = possiblePlacements;
            Iterator<StoneType> iterator = possiblePlacements.listIterator();
            while (iterator.hasNext()) {
                if (!types.contains(iterator.next())) {
                    iterator.remove();
                }
            }
            this.score = this.score + score;
        }

    }

    public String toString() {
        return possiblePlacements.toString() + " score: " + score;
    }
}
