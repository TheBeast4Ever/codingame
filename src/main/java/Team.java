import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class Team {
    int score;
    Collection<Entity> robots;

    void readScore(Scanner in) {
        score = in.nextInt();
        robots = new ArrayList<Entity>();
    }
}
