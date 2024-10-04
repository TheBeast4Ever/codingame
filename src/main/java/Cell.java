import java.util.Scanner;

public class Cell {
    boolean known;
    int ore;
    boolean hole;

    Cell(boolean known, int ore, boolean hole) {
        this.known = known;
        this.ore = ore;
        this.hole = hole;
    }

    Cell(Scanner in) {
        String oreStr = in.next();
        if (oreStr.charAt(0) == '?') {
            known = false;
            ore = 0;
        } else {
            known = true;
            ore = Integer.parseInt(oreStr);
        }
        String holeStr = in.next();
        hole = (holeStr.charAt(0) != '0');
    }
}
