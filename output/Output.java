//Version Fri Oct 04 18:46:26 CEST 2024
import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import static java.lang.Math.*;
class DigHereForOreRule implements IRule{
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentCell.ore>0 && currentRobot.item.equals(EntityType.NOTHING)) {
            action.setEfficiency(99);
            System.err.println("Hum, it seems good to search ore here : (" + currentRobot.pos.x + " ; " + currentRobot.pos.y + "). Ore value is : " + currentCell.ore);
        } else {
            if (currentRobot.pos.x!=0 && !currentCell.hole) {
                action.setEfficiency(50);
            } else {
                action.setEfficiency(0);
            }
        }
        return action;
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
            board.myVisibleOrePos.stream().forEach(currCord -> {
                double currentDistance = currCord.distance(currentRobot.pos);
                if (currentDistance< bestDistance[0]) {
                    bestDistance[0] = currentDistance;
                    bestCoordToFollow[0] = currCord;
                }
            });
            coordToFollow=bestCoordToFollow[0];
            efficiency[0] =90;
            System.err.println("Hey, it smells ore at : (" + coordToFollow + ")");
        }
        Action action = Action.move(coordToFollow);
        action.setEfficiency(efficiency[0]);
        return action;
    }
}

class Entity {
    private static final Coord DEAD_POS = new Coord(-1, -1);

    // Updated every turn
    final int id;
    final EntityType type;
    final Coord pos;
    final EntityType item;

    // Computed for my robots
    Action action;

    Entity(Scanner in) {
        id = in.nextInt();
        type = EntityType.valueOf(in.nextInt());
        pos = new Coord(in);
        item = EntityType.valueOf(in.nextInt());
    }

    boolean isAlive() {
        return !DEAD_POS.equals(pos);
    }
}
class RequestRadarRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.request(EntityType.RADAR);
        if (currentRobot.pos.x!=0) {
            action.setEfficiency(0);
        } else {
            if (board.myRadarCooldown==0 && currentRobot.item.equals(EntityType.NOTHING)) {
                action.setEfficiency(60);
            } else {
                action.setEfficiency(0);
            }
        }

        return action;
    }
}
class RequestTrapRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.request(EntityType.TRAP);
        if (currentRobot.pos.x!=0) {
            action.setEfficiency(0);
        } else {
            if (board.myTrapCooldown==0 && currentRobot.item.equals(EntityType.NOTHING)) {
                action.setEfficiency(60);
            } else {
                action.setEfficiency(0);
            }
        }

        return action;
    }
}

class ActionDecider {
    private List<IRule> rules = new ArrayList<IRule>();
    
    public ActionDecider() {
        // Add rules here
        rules.add(new RandomMoveRule());
        rules.add(new FollowOreFoundMoveRule());
        rules.add(new BackToHeadQuarterRule());
        rules.add(new DigHereForOreRule());
        rules.add(new DigHereForPutRadarRule());
        rules.add(new RequestRadarRule());
        rules.add(new RequestTrapRule());
    }
    
    public Action identifyBestActionToPerform(Board board, Entity allyRobot) {
        Action bestPossibleAction = Action.none();
        int bestEfficiency=0;
        for (IRule rule:rules) {
            Action currentAction = rule.evaluateAction(board, allyRobot);
            if (currentAction.getEfficiency()>bestEfficiency) {
                bestPossibleAction = currentAction;
                bestEfficiency = currentAction.getEfficiency();
            }
        }
        return bestPossibleAction;
    }
}
enum EntityType {
    NOTHING, ALLY_ROBOT, ENEMY_ROBOT, RADAR, TRAP, AMADEUSIUM;

    static EntityType valueOf(int id) {
        return values()[id + 1];
    }
}

class Cell {
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
class Action {
    public int getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(int efficiency) {
        this.efficiency = efficiency;
    }

    private int efficiency;
    final String command;
    final Coord pos;
    final EntityType item;
    String message;

    private Action(String command, Coord pos, EntityType item) {
        this.command = command;
        this.pos = pos;
        this.item = item;
    }

    static Action none() {
        return new Action("WAIT", null, null);
    }

    static Action move(Coord pos) {
        return new Action("MOVE", pos, null);
    }

    static Action dig(Coord pos) {
        return new Action("DIG", pos, null);
    }

    static Action request(EntityType item) {
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
}
class DigHereForPutRadarRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentRobot.pos.x!=0 && currentRobot.item.equals(EntityType.RADAR) && !currentCell.known) {
            action.setEfficiency(90);
            System.err.println("Hum, it seems good to put a radar here : (" + currentRobot.pos.x + " ; " + currentRobot.pos.y + ")");
        } else {
            action.setEfficiency(0);
        }
        return action;
    }
}

class Board {
    // Given at startup
    final int width;
    final int height;

    // Updated each turn
    final Team myTeam = new Team();
    final Team opponentTeam = new Team();
    private Cell[][] cells;
    int myRadarCooldown;
    int myTrapCooldown;
    Map<Integer, Entity> entitiesById;
    Collection<Coord> myRadarPos;
    Collection<Coord> myTrapPos;

    Collection<Coord> myVisibleOrePos;

    Board(Scanner in) {
        width = in.nextInt();
        height = in.nextInt();
    }

    void update(Scanner in) {
        // Read new data
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

    boolean cellExist(Coord pos) {
        return (pos.x >= 0) && (pos.y >= 0) && (pos.x < width) && (pos.y < height);
    }

    Cell getCell(Coord pos) {
        return cells[pos.y][pos.x];
    }
}
class DigHereForPutTrapRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentRobot.pos.x!=0 && currentRobot.item.equals(EntityType.TRAP) && currentCell.known && currentCell.ore==0) {
            action.setEfficiency(90);
            System.err.println("Hum, it seems good to put a trap here : (" + currentRobot.pos.x + " ; " + currentRobot.pos.y + ")");
        } else {
            action.setEfficiency(0);
        }
        return action;
    }
}
class BackToHeadQuarterRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action=Action.move(new Coord(0, currentRobot.pos.y));

        if (currentRobot.item.equals(EntityType.AMADEUSIUM)) {
            action.setEfficiency(100);
            System.err.println("Hey, i've ore, go back to head quarter : (" + currentRobot.pos.x + " ; " + currentRobot.pos.y + ")");
        } else {
            action.setEfficiency(0);
        }
        return action;
    }
}

class RandomMoveRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Random rand = new Random();
        int maxX=board.width,maxY= board.height;
        Action action = Action.move(new Coord(rand.nextInt(maxX+1)-1, rand.nextInt(maxY+1)-1));
        action.setEfficiency(10);
        return action;
    }
}

class Team {
    int score;
    Collection<Entity> robots;

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
            int nbofAliveRobots = board.myTeam.robots.size();
            int nbOfDeadRobots = 5-nbofAliveRobots;
            ActionDecider decider = new ActionDecider();
            board.myTeam.robots.stream().filter(currentRobot-> currentRobot.isAlive()).forEach(currentRobot -> {
                Action currAction = decider.identifyBestActionToPerform(board, currentRobot);
                System.err.println("Robot " + currentRobot.id + " is performing this action : " + currAction.toString() + " (" + currAction.getEfficiency() + ")");
                System.out.println(currAction);
            });
            board.myTeam.robots.stream().filter(currentRobot-> !currentRobot.isAlive()).forEach(currentRobot -> {
                System.err.println("I'm dead : " + currentRobot.id);
                System.out.println(Action.none());
            });
        }
    }
}
interface IRule {
    Action evaluateAction(Board board, Entity currentRobot);
}

class Coord {
    final int x;
    final int y;

    Coord(int x, int y) {
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
    int distance(Coord other) {
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
