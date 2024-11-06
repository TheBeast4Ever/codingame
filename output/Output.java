//Version Wed Nov 06 23:16:53 CET 2024
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
            action.efficiencyRate=EfficiencyRate.USELESS;
        } else {
            if (board.myTrapCooldown==0 && currentRobot.item.equals(EntityType.NOTHING)) {
                action.efficiencyRate=EfficiencyRate.MAXIMUM;
            } else {
                action.efficiencyRate=EfficiencyRate.USELESS;
            }
        }

        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("REQ T");
    }
}


class FollowOreFoundMoveRule implements IRule {

    private boolean isOreFoundAnywhere(Board board) {
        Message message = board.hub.consumeWithoutRemove();
        if (message != null) {
            if (message.header.equals("ORE-FOUND")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Coord coordToFollow = new Coord(0,0);
        final EfficiencyRate[] efficiency = {EfficiencyRate.USELESS};
        if ((!board.myVisibleOrePos.isEmpty()) && currentRobot.item.equals(EntityType.NOTHING)) {
            final Coord[] bestCoordToFollow = {new Coord(0, 0)};
            final double[] bestDistance = {Integer.MAX_VALUE};
            board.myVisibleOrePos.stream().filter(pos-> !board.myTrapPos.contains(pos)).forEach(currCord -> {
                double currentDistance = currCord.distance(currentRobot.pos);
                if (currentDistance< bestDistance[0]) {
                    bestDistance[0] = currentDistance;
                    bestCoordToFollow[0] = currCord;
                    efficiency[0] = EfficiencyRate.HIGH;
                }
            });
            coordToFollow=bestCoordToFollow[0];
            System.err.println("best to follow : " + coordToFollow);
        } else if (isOreFoundAnywhere(board) && currentRobot.item.equals(EntityType.NOTHING)) {
            Message message = board.hub.consumeAndRemove();
            coordToFollow = message.pos;
            efficiency[0] = EfficiencyRate.HIGH;
        }
        Action action = Action.move(coordToFollow);
        action.efficiencyRate = efficiency[0];
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("FOFM");
    }
}


class DigHereForPutTrapRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentRobot.pos.x!=0 && currentRobot.item.equals(EntityType.TRAP) && currentCell.known && currentCell.ore==1  && !board.myTrapPos.contains(currentRobot.pos)) {
            action.efficiencyRate = EfficiencyRate.HIGH;
        } else {
            action.efficiencyRate = EfficiencyRate.USELESS;
        }
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("DHFPT");
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

    public Collection<Coord> myPossibleTrapPositions;

    public Collection<Coord> myVisibleOrePos;

    public Collection<Coord> myUnsuccessfulHoles = new ArrayList<Coord>();

    public Integer roundNumber;

    public MessagesHub hub;

    public Board(Scanner in) {
        width = in.nextInt();
        height = in.nextInt();
        roundNumber=0;
        hub = new MessagesHub();
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

    public EfficiencyRate efficiencyRate;
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
        return Integer.compare(this.efficiencyRate.getValue(),otherAction.efficiencyRate.getValue());
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
        action.efficiencyRate=EfficiencyRate.WEAK;
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("RM");
    }
}


class DigForOreRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentRobot.item.equals(EntityType.NOTHING) && currentRobot.pos.x != 0 && !board.myUnsuccessfulHoles.contains(currentRobot.pos)) {
            if (currentCell.ore > 0 && !board.myTrapPos.contains(currentRobot.pos)) {
                action.efficiencyRate = EfficiencyRate.MAXIMUM;
            } else {
                if (!board.myTrapPos.contains(currentRobot.pos) && !currentCell.hole) {
                    action.efficiencyRate = EfficiencyRate.AVERAGE;
                } else {
                    if (!board.myTrapPos.contains(currentRobot.pos)) {
                        action.efficiencyRate = EfficiencyRate.WEAK;
                    } else {
                        action.efficiencyRate = EfficiencyRate.USELESS;
                    }
                }
            }
        } else {
            action.efficiencyRate = EfficiencyRate.USELESS;
        }
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("DIG");
    }
}

enum EfficiencyRate {
    USELESS(0), WEAK(10), AVERAGE(50), HIGH(90), MAXIMUM(100);
    private final Integer efficiency;

    private EfficiencyRate(Integer efficiency) {
        this.efficiency = efficiency;
    }

    public Integer getValue() {
        return efficiency;
    }
}



class ActionDecider {
    private List<IRule> rules = new ArrayList<IRule>();
    
    public ActionDecider() {
        // Add rules here
        rules.add(new RandomMoveRule());
        // rules.add(new RandomMoveFor100FirstRoundsRule());
        // rules.add(new KeepPreviousActionRule());
        rules.add(new FollowOreFoundMoveRule());
        rules.add(new BackToHeadQuarterRule());
        rules.add(new DigForOreRule());
        // rules.add(new DigHereForPutRadarRule());
        // rules.add(new DigHereForPutTrapRule());
        // rules.add(new RequestRadarRule());
        // rules.add(new SmartKamikazeRule());
        // rules.add(new GoToBestPlaceToPutRadarRule());
        // rules.add(new RequestTrapRule());
    }

    public List<Action> computeEligibleActionsRankedByEfficiency(Board board, Entity allyRobot) {
        List<Action> actionsList = new ArrayList<>();
        for (IRule rule:rules) {
            Action actionComputed = rule.evaluateAction(board, allyRobot);
            if (actionComputed.efficiencyRate.getValue()>EfficiencyRate.USELESS.getValue()) {
                actionsList.add(actionComputed);
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
        EfficiencyRate efficiencyRateToCompute = EfficiencyRate.USELESS;

        if (currentRobot.item.equals(EntityType.AMADEUSIUM)) {
            efficiencyRateToCompute= EfficiencyRate.MAXIMUM;

        }
        action.efficiencyRate = efficiencyRateToCompute;
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("HQ");
    }


}


class RequestRadarRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.request(EntityType.RADAR);
        if (currentRobot.pos.x!=0) {
            action.efficiencyRate=EfficiencyRate.USELESS;
        } else {
            if (board.myRadarCooldown==0 && currentRobot.item.equals(EntityType.NOTHING)) {
                action.efficiencyRate=EfficiencyRate.MAXIMUM;
            } else {
                action.efficiencyRate=EfficiencyRate.USELESS;
            }
        }

        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("REQ R");
    }
}


class DigHereForPutRadarRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentRobot.pos.x>0 && currentRobot.item.equals(EntityType.RADAR)  && !currentCell.hole  && !currentCell.known && !board.myTrapPos.contains(currentRobot.pos)) {
            action.efficiencyRate = EfficiencyRate.HIGH;
        } else {
            action.efficiencyRate = EfficiencyRate.USELESS;
        }
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("DHFPR");
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
    public Action previousAction;

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


class MessagesHub {
    private Map<Integer, List<Message>> oldMessages;

    private Queue<Message> messages;

    public MessagesHub() {
        messages = new ArrayDeque<Message>();
        oldMessages = new HashMap<Integer, List<Message>>();
    }

    public void pub(Message message) {
        messages.add(message);
    }

    public Message consumeWithoutRemove() {
        return messages.peek();
    }
    public Message consumeAndRemove() {
        return messages.poll();
    }

    public void oldPub(Message message) {
        List<Message> messageForSamePriority = oldMessages.containsKey(message.priority)? oldMessages.get(message.priority):new ArrayList<Message>();
        messageForSamePriority.add(message);
        oldMessages.put(message.priority, messageForSamePriority);
        System.err.println("New message : " + message);
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



class SmartKamikazeRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.none();
        if (currentRobot.id == 0 && board.roundNumber<83) {
            if (currentRobot.pos.x==0 && board.myTrapCooldown==0 && currentRobot.item.equals(EntityType.NOTHING) && !board.myPossibleTrapPositions.isEmpty()) {
                action = Action.request(EntityType.TRAP);
            } else if (currentRobot.item.equals(EntityType.TRAP) && !board.myPossibleTrapPositions.isEmpty()) {
                final Coord[] bestCoordToFollow = {new Coord(0, currentRobot.pos.y)};
                final double[] bestDistance = {Integer.MAX_VALUE};

                board.myPossibleTrapPositions.stream().forEach(currCord -> {
                    double currentDistance = currCord.distance(currentRobot.pos);
                    if (currentDistance< bestDistance[0]) {
                        bestDistance[0] = currentDistance;
                        bestCoordToFollow[0] = currCord;
                    }
                });
                action = Action.dig(bestCoordToFollow[0]);
            } else if (board.myTrapCooldown==0 && !board.myPossibleTrapPositions.isEmpty()) {
                action = Action.move(new Coord(0,currentRobot.pos.y));
            } else if (board.myPossibleTrapPositions.isEmpty()){
                // mode veille
                action = Action.move(new Coord(3,currentRobot.pos.y));
            } else {
                // mode veille
                action = Action.none();
            }
            action.efficiencyRate=EfficiencyRate.MAXIMUM;
        } else {
            action.efficiencyRate=EfficiencyRate.USELESS;
        }

        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("SK");
    }
}


class Player {
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Board board = new Board(in);

        Map<Integer, Action> myPreviousActionsByRobot = new HashMap<Integer, Action>();
        Collection<Coord> emptyHoles = new ArrayList<Coord>();

        // game loop
        while (true) {
            board.update(in);

            ActionDecider decider = new ActionDecider();
            Entity[] robots = board.myTeam.robots.toArray(new Entity[board.myTeam.robots.size()]);
            Map<Integer, Action> actionsToPlay = new HashMap<Integer, Action>();

            for(int i=0;i<5;i++) {
                Entity currentRobot = robots[i];
                if (myPreviousActionsByRobot.containsKey(currentRobot.id)) {
                    currentRobot.previousAction = myPreviousActionsByRobot.get(currentRobot.id);
                    if (currentRobot.previousAction.command.equals("DIG")) {
                        if (currentRobot.item.equals(EntityType.AMADEUSIUM)) {
                            board.hub.pub(new Message("ORE-FOUND", "Ore has been found at this position : " + currentRobot.pos, currentRobot.pos,1));
                        } else if (currentRobot.item.equals(EntityType.NOTHING)) {
                            emptyHoles.add(currentRobot.pos);
                        }
                    }
                }
            }

            board.myUnsuccessfulHoles = emptyHoles;

            for(int i=0;i<5;i++) {
                Entity currentRobot = robots[i];

                if (currentRobot.isAlive()) {
                    List<Action> possibleActions = decider.computeEligibleActionsRankedByEfficiency(board, currentRobot);
                    Optional<Action> bestActionToPerform = possibleActions.stream()
                            .filter(a->!actionsToPlay.containsValue(a) || a.efficiencyRate== EfficiencyRate.MAXIMUM).findFirst();
                    if (bestActionToPerform.isPresent()) {
                        myPreviousActionsByRobot.put(currentRobot.id, bestActionToPerform.get());
                        actionsToPlay.put(currentRobot.id, bestActionToPerform.get());
                        System.err.println("Robot " + currentRobot.id + " perform action with efficiency " + bestActionToPerform.get().efficiencyRate);
                        System.out.println(bestActionToPerform.get());
                    } else  {
                        System.err.println("Strange for #" + currentRobot.id);
                        myPreviousActionsByRobot.remove(currentRobot.id);
                        System.out.println(Action.none());
                    }
                } else {
                    myPreviousActionsByRobot.remove(currentRobot.id);
                    System.out.println(Action.none());
                }
            }
        }
    }
}


class KeepPreviousActionRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = currentRobot.previousAction;
        if (action != null) {
            return action;
        } else {
            action = Action.none();
            action.efficiencyRate=EfficiencyRate.USELESS;
            return action;
        }
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("KPA");
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


class Message {
    private static Integer idSequence = 0;

    public Integer id;

    public String header;
    public String content;

    public Coord pos;

    public Integer priority;

    public Message(String header, String content, Coord pos, Integer priority) {
        this.id = idSequence++;
        this.header = header;
        this.content = content;
        this.pos = pos;
        this.priority = priority;
    }


    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", header='" + header + '\'' +
                ", content='" + content + '\'' +
                ", priority=" + priority +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return priority.equals(message.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority);
    }
}


class GoToBestPlaceToPutRadarRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Coord coordToFollow = new Coord(0,0);
        EfficiencyRate efficiencyRate = EfficiencyRate.USELESS;
        if (currentRobot.item.equals(EntityType.RADAR)) {
            efficiencyRate=EfficiencyRate.HIGH;
            Coord bestCoordToFollow = new Coord(currentRobot.pos.x, currentRobot.pos.y);
            double bestDistance = Integer.MAX_VALUE;
            for (int x=1; x < board.width; x++) {
                for (int y=0; y < board.height; y++) {
                    Coord currCord = new Coord(x,y);
                    if (!board.getCell(currCord).known && !board.myTrapPos.contains(currCord)) {
                        double currentDistance = currCord.distance(currentRobot.pos);
                        if (currentDistance<bestDistance) {
                            bestDistance = currentDistance;
                            bestCoordToFollow = currCord;
                        }
                    }

                }
            }

            if (bestCoordToFollow.equals(new Coord(currentRobot.pos.x, currentRobot.pos.y))){
                efficiencyRate=EfficiencyRate.USELESS;
            }

            coordToFollow=bestCoordToFollow;
            System.err.println("best move to put radar : " + coordToFollow);
        }
        Action action = Action.move(coordToFollow);
        action.efficiencyRate = efficiencyRate;
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return "GBPR";
    }
}


interface IRule {

    Action evaluateAction(Board board, Entity currentRobot);

    String getMessage(Entity currentRobot);
}
