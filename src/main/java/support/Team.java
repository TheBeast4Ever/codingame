package support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class Team {
    public int score;
    public Collection<Entity> robots;

    void readScore(Scanner in) {
        score = in.nextInt();
        robots = new ArrayList<Entity>();
    }
}
