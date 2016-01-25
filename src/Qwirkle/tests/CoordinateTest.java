package qwirkle.tests;

import qwirkle.gamelogic.Coordinate;

public class CoordinateTest {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        System.out.println(Coordinate.getCoordinateHash(-132, 22));
        System.out.println(Coordinate.getCoordinates("N:132:P:22")[0] + ", " + Coordinate
            .getCoordinates("N:132:P:22")[1]);
    }

}
