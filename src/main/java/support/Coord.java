package support;

import static java.lang.Math.*;
import java.util.Scanner;

public class Coord {
    public final int x;
    public final int y;

    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Coord(Scanner in) {
        this(in.nextInt(), in.nextInt());
    }

    Coord add(Coord other) {
        return new Coord(x + other.x, y + other.y);
    }

    // Manhattan distance (for 4 directions maps)
    // see: https://en.wikipedia.org/wiki/Taxicab_geometry
    public int distance(Coord other) {
        return abs(x - other.x) + abs(y - other.y);
    }

    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + x;
        result = PRIME * result + y;
        return result;
    }

    public double computeDistanceFrom(Coord c) {
        double distance=0;
        int diffX = Math.abs(this.x-c.x);
        int diffY = Math.abs(this.y-c.y);
        distance = Math.sqrt(diffX*diffX + diffY*diffY);
        return distance;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Coord other = (Coord) obj;
        return (x == other.x) && (y == other.y);
    }

    public String toString() {
        return x + " " + y;
    }
}
