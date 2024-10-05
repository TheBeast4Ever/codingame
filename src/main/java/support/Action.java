package support;

import java.util.Objects;

public class Action implements Comparable<Action> {

    public int efficiency;
    public final String command;
    public final Coord pos;
    public final EntityType item;
    public String message;

    private Action(String command, Coord pos, EntityType item) {
        this.command = command;
        this.pos = pos;
        this.item = item;
    }

    public static Action none() {
        return new Action("WAIT", null, null);
    }

    public static Action move(Coord pos) {
        return new Action("MOVE", pos, null);
    }

    public static Action dig(Coord pos) {
        return new Action("DIG", pos, null);
    }

    public static Action request(EntityType item) {
        return new Action("REQUEST", null, item);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(command);
        if (pos != null) {
            builder.append(' ').append(pos);
        }
        if (item != null) {
            builder.append(' ').append(item);
        }
        if (message != null) {
            builder.append(' ').append(message);
        }
        return builder.toString();
    }

    @Override
    public int compareTo(Action otherAction) {
        return Integer.compare(this.efficiency,otherAction.efficiency);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Action action = (Action) o;
        return Objects.equals(command, action.command) && Objects.equals(pos, action.pos) && item == action.item;
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, pos, item);
    }
}
