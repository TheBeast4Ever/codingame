//Version Sat Oct 05 18:59:25 CEST 2024
import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import static java.lang.Math.*;


class RequestTrapRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.request(EntityType.TRAP);
        if (currentRobot.pos.x!=0) {
            action.efficiency=0;
        } else {
            if (board.myTrapCooldown==0 && currentRobot.item.equals(EntityType.NOTHING)) {
                action.efficiency=60;
            } else {
                action.efficiency=0;
            }
        }

        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("It's time to get a trap (#" + currentRobot.id + ")");
    }
}


class FollowOreFoundMoveRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Coord coordToFollow = new Coord(0,0);
        final int[] efficiency = {0};
        if (!board.myVisibleOrePos.isEmpty() && currentRobot.item.equals(EntityType.NOTHING)) {
            final Coord[] bestCoordToFollow = {new Coord(0, 0)};
            final double[] bestDistance = {Integer.MAX_VALUE};
            board.myVisibleOrePos.stream().filter(pos-> !board.myTrapPos.contains(pos)).forEach(currCord -> {
                double currentDistance = currCord.distance(currentRobot.pos);
                if (currentDistance< bestDistance[0]) {
                    bestDistance[0] = currentDistance;
                    bestCoordToFollow[0] = currCord;
                    efficiency[0] = 90;
                }
            });
            coordToFollow=bestCoordToFollow[0];
        }
        Action action = Action.move(coordToFollow);
        action.efficiency = efficiency[0];
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("Hey, it smells ore (#" + currentRobot.id + ")");
    }
}


class DigHereForPutTrapRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentRobot.pos.x!=0 && currentRobot.item.equals(EntityType.TRAP) && currentCell.known && currentCell.ore==0  && !board.myTrapPos.contains(currentRobot.pos)) {
            action.efficiency = 90;
        } else {
            action.efficiency = 0;
        }
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("Hum, it seems good to put a trap here : (" + currentRobot.pos.x + " ; " + currentRobot.pos.y + ")");
    }
}


class Board {
    // Given at startup
    public final int width;
    public final int height;

    // Updated each turn
    public final Team myTeam = new Team();
    public final Team opponentTeam = new Team();
    private Cell[][] cells;
    public int myRadarCooldown;
    public int myTrapCooldown;
    public Map<Integer, Entity> entitiesById;
    public Collection<Coord> myRadarPos;
    public Collection<Coord> myTrapPos;

    public Collection<Coord> myVisibleOrePos;

    public Integer roundNumber;

    public Board(Scanner in) {
        width = in.nextInt();
        height = in.nextInt();
        roundNumber=0;
    }

    public void update(Scanner in) {
        // Read new data
        roundNumber++;
        myTeam.readScore(in);
        opponentTeam.readScore(in);
        cells = new Cell[height][width];
        myVisibleOrePos = new ArrayList<Coord>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = new Cell(in);
                for (int i =0; i<cells[y][x].ore; i++) {
                    myVisibleOrePos.add(new Coord(x,y));
                }
            }
        }
        int entityCount = in.nextInt();
        myRadarCooldown = in.nextInt();
        myTrapCooldown = in.nextInt();
        entitiesById = new HashMap<Integer, Entity>();
        myRadarPos = new ArrayList<Coord>();
        myTrapPos = new ArrayList<Coord>();
        for (int i = 0; i < entityCount; i++) {
            Entity entity = new Entity(in);
            entitiesById.put(entity.id, entity);
            if (entity.type == EntityType.ALLY_ROBOT) {
                myTeam.robots.add(entity);
            } else if (entity.type == EntityType.ENEMY_ROBOT) {
                opponentTeam.robots.add(entity);
            } else if (entity.type == EntityType.RADAR) {
                myRadarPos.add(entity.pos);
            } else if (entity.type == EntityType.TRAP) {
                myTrapPos.add(entity.pos);
            }
        }
    }

    public boolean cellExist(Coord pos) {
        return (pos.x >= 0) && (pos.y >= 0) && (pos.x < width) && (pos.y < height);
    }

    public Cell getCell(Coord pos) {
        return cells[pos.y][pos.x];
    }
}


class Cell {
    public boolean known;
    public int ore;
    public boolean hole;

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


class Action implements Comparable<Action> {

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



class RandomMoveRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Random rand = new Random();
        int maxX=board.width,maxY= board.height;
        Action action = Action.move(new Coord(rand.nextInt(maxX+1)-1, rand.nextInt(maxY+1)-1));
        action.efficiency=10;
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("My brain is off (#" + currentRobot.id + ")");
    }
}



class ActionDecider {
    private List<IRule> rules = new ArrayList<IRule>();
    
    public ActionDecider() {
        // Add rules here
        rules.add(new RandomMoveRule());
        rules.add(new RandomMoveFor50FirstRoundsRule());
        rules.add(new FollowOreFoundMoveRule());
        rules.add(new BackToHeadQuarterRule());
        rules.add(new DigHereForOreRule());
        rules.add(new DigHereForPutRadarRule());
        rules.add(new DigHereForPutTrapRule());
        rules.add(new RequestRadarRule());
        rules.add(new RequestTrapRule());
    }
    
    public Action identifyBestActionToPerform(Board board, Entity allyRobot) {
        Action bestPossibleAction = Action.none();
        int bestEfficiency=0;
        for (IRule rule:rules) {
            Action currentAction = rule.evaluateAction(board, allyRobot);
            if (currentAction.efficiency>bestEfficiency) {
                bestPossibleAction = currentAction;
                bestEfficiency = currentAction.efficiency;
            }
        }
        return bestPossibleAction;
    }

    public List<Action> getPossibleActionsSortedByEfficiency(Board board, Entity allyRobot) {
        List<Action> actionsList = new ArrayList<>();
        for (IRule rule:rules) {
            Action currentAction = rule.evaluateAction(board, allyRobot);
            if (currentAction.efficiency>0) {
                actionsList.add(currentAction);
            }
        }
        Collections.reverse(actionsList);
        return actionsList;
    }
}



class BackToHeadQuarterRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.move(new Coord(0, currentRobot.pos.y));
        int efficiency=0;

        if (currentRobot.item.equals(EntityType.AMADEUSIUM)) {
            efficiency=100;

        }
        action.efficiency = efficiency;
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("Hey, i've ore, go back to head quarter : (" + currentRobot.pos.x + " ; " + currentRobot.pos.y + ")");
    }


}


class RequestRadarRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.request(EntityType.RADAR);
        if (currentRobot.pos.x!=0) {
            action.efficiency=0;
        } else {
            if (board.myRadarCooldown==0 && currentRobot.item.equals(EntityType.NOTHING)) {
                action.efficiency=60;
            } else {
                action.efficiency=0;
            }
        }

        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("It's time to get a radar (#" + currentRobot.id + ")");
    }
}


class DigHereForPutRadarRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentRobot.pos.x!=0 && currentRobot.item.equals(EntityType.RADAR) && !currentCell.known  && !board.myTrapPos.contains(currentRobot.pos)) {
            action.efficiency = 90;
        } else {
            action.efficiency = 0;
        }
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("Hum, it seems good to put a radar here : (" + currentRobot.pos.x + " ; " + currentRobot.pos.y + ")");
    }
}


class Entity {
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



class RandomMoveFor50FirstRoundsRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Random rand = new Random();
        int offset=15;
        int maxX=board.width-offset,maxY= board.height;
        Action action = Action.move(new Coord(offset+rand.nextInt(maxX+1)-1, rand.nextInt(maxY+1)-1));
        int efficiency=0;
        if (board.roundNumber<=50) {
            efficiency=20;
        }
        action.efficiency = efficiency;
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("My brain is off for the first 50 rounds (#" + currentRobot.id + ")");
    }
}


class Team {
    public int score;
    public Collection<Entity> robots;

    void readScore(Scanner in) {
        score = in.nextInt();
        robots = new ArrayList<Entity>();
    }
}


class Player {
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Board board = new Board(in);

        // game loop
        while (true) {
            board.update(in);

            ActionDecider decider = new ActionDecider();
            Entity[] robots = board.myTeam.robots.toArray(new Entity[board.myTeam.robots.size()]);
            Map<Integer, Action> actionsToPlay = new HashMap<Integer, Action>();
            for(int i=0;i<5;i++) {
                Entity currentRobot =  robots[i];
                if (currentRobot.isAlive()) {
                    List<Action> possibleActions = decider.getPossibleActionsSortedByEfficiency(board, currentRobot);
                    System.err.println("Robot " + currentRobot.id + " has " + possibleActions.size() + " possible actions");
                    Optional<Action> bestActionToPerform = possibleActions.stream().filter(a->!actionsToPlay.containsValue(a)).findFirst();
                    if (bestActionToPerform.isPresent()) {
                        actionsToPlay.put(currentRobot.id, bestActionToPerform.get());
                        System.out.println(bestActionToPerform.get());
                    } else  {
                        System.err.println("Strange for #" + currentRobot.id);
                        System.out.println(Action.none());
                    }
                } else {
                    System.err.println("End of the game for robot #" + currentRobot.id);
                    System.out.println(Action.none());
                }
            }
        }
    }
}


class Coord {
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

enum EntityType {
    NOTHING, ALLY_ROBOT, ENEMY_ROBOT, RADAR, TRAP, AMADEUSIUM;

    static EntityType valueOf(int id) {
        return values()[id + 1];
    }
}


interface IRule {
    Action evaluateAction(Board board, Entity currentRobot);

    String getMessage(Entity currentRobot);
}


class DigHereForOreRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentCell.ore>0 && currentRobot.item.equals(EntityType.NOTHING) && !board.myTrapPos.contains(currentRobot.pos)) {
            action.efficiency = 99;
        } else {
            if (currentRobot.pos.x!=0 && !currentCell.hole && !board.myTrapPos.contains(currentRobot.pos)) {
                action.efficiency = 50;
            } else {
                action.efficiency = 0;
            }
        }
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("Hum, it seems good to search ore here : (" + currentRobot.pos.x + " ; " + currentRobot.pos.y + ")");
    }
}
