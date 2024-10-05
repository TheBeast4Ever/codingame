package support;

import java.util.Scanner;

public class Entity {
    private static final Coord DEAD_POS = new Coord(-1, -1);

    // Updated every turn
    public final int id;
    public final EntityType type;
    public final Coord pos;
    public final EntityType item;

    // Computed for my robots
    Action action;

    public Entity(Scanner in) {
        id = in.nextInt();
        type = EntityType.valueOf(in.nextInt());
        pos = new Coord(in);
        item = EntityType.valueOf(in.nextInt());
    }

    public boolean isAlive() {
        return !DEAD_POS.equals(pos);
    }
}
