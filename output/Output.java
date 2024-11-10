//Version Sun Nov 10 19:58:00 CET 2024
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
        if (board.myTrapCooldown==0) {
            System.err.println("near " + board.whoIsMyAllyNearestFromHeadQuarter().get().id);
        }

        if (board.myTrapCooldown==0
                && currentRobot.item.equals(EntityType.NOTHING)
                && board.whoIsMyAllyNearestFromHeadQuarter().get().id == currentRobot.id) {
            if (currentRobot.pos.x != 0) {
                action = Action.move(new Coord(0,currentRobot.pos.y));
            }
            action.efficiencyRate=EfficiencyRate.HIGH;
        } else {
            action.efficiencyRate=EfficiencyRate.USELESS;
        }

        action.message = getMessage();
        return action;
    }

    @Override
    public String getMessage() {
        return ("REQ T");
    }
}


class FollowOreFoundWithoutRadarRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Coord coordToFollow = new Coord(0,0);
        final EfficiencyRate[] efficiency = {EfficiencyRate.USELESS};

        if (messageOreFoundPublished(board)
                && currentRobot.item.equals(EntityType.NOTHING)
                && board.isThisPosIsSafe(board.hub.consumeWithoutRemove().pos)
                && board.whoIsMyAllyNearestFromThisCoordWithoutItem(board.hub.consumeWithoutRemove().pos, EntityType.AMADEUSIUM).get().equals(currentRobot)) {
            System.err.println("msg consumed by " + currentRobot.id);
            Message message = board.hub.consumeAndRemove();
            coordToFollow = message.pos;
            efficiency[0] = EfficiencyRate.MAXIMUM;
        } else if (currentRobot.previousAction != null
                && currentRobot.previousAction.message.equals(getMessage())
                && currentRobot.item.equals(EntityType.NOTHING)) {
            coordToFollow = currentRobot.previousAction.pos;
            efficiency[0] = EfficiencyRate.MAXIMUM;
        }
        Action action = Action.move(coordToFollow);

        if (coordToFollow.equals(currentRobot.pos)
                && board.isThisPosIsSafe(currentRobot.pos)) {
            action = Action.dig(coordToFollow);
            action.message = "DIG 2";
        } else {
            action.message = getMessage();
        }
        action.efficiencyRate = efficiency[0];


        return action;
    }

    @Override
    public String getMessage() {
        return "FOF2";
    }

    private boolean messageOreFoundPublished(Board board) {
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

    public boolean isTargetAccessibleFromActualPosition(Coord target, Coord actualPos) {
        List<Coord> coords = this.getAllCoordsAccessibleFrom(actualPos);
        return coords.contains(target);
    }

    public List<Coord> getAllCoordsAccessibleFrom(Coord pos) {
        final int MAX_DISTANCE = 4;
        List<Coord> coords = new ArrayList<>();
        int minX = Math.max((pos.x - MAX_DISTANCE), 0);
        int minY = Math.max((pos.y - MAX_DISTANCE), 0);

        int maxX = Math.min((pos.x + MAX_DISTANCE), width-1);
        int maxY = Math.min((pos.y + MAX_DISTANCE), height-1);

        for (int i=minX ; i<=maxX ; i++) {
            for (int j=minY ; j<=maxY ; j++) {
                Coord currentCoord = new Coord(i,j);
                int distanceFromPos = pos.distance(currentCoord);
                if (distanceFromPos<=MAX_DISTANCE) {
                    coords.add(currentCoord);
                }
            }
        }

        return coords;
    }

    public Coord getNearestIdealRadarPosition(Coord actualPosition) {
        Coord nearestPosition = actualPosition;

        double bestDistance = Integer.MAX_VALUE;
        for (int x=2; x < width-1; x=x+5) {
            for (int y=3; y < height-1; y=y+5) {
                Coord currCord = new Coord(x,y);
                if (!myTrapPos.contains(currCord) && !myRadarPos.contains(currCord)) {
                    double currentDistance = currCord.distance(actualPosition);
                    if (currentDistance<bestDistance) {
                        bestDistance = currentDistance;
                        nearestPosition = currCord;
                    }
                }
            }
        }

        return nearestPosition;
    }

    public Coord getNearestIdealTrapPosition(Coord actualPosition) {
        Coord nearestPosition = actualPosition;

        double bestDistance = Integer.MAX_VALUE;
        for (int x=2; x < width-1; x=x+5) {
            for (int y=3; y < height-1; y=y+5) {
                Coord currCord = new Coord(x,y);
                if (!myTrapPos.contains(currCord)) {
                    double currentDistance = currCord.distance(actualPosition);
                    if (currentDistance<bestDistance) {
                        bestDistance = currentDistance;
                        nearestPosition = currCord;
                    }
                }
            }
        }

        return nearestPosition;
    }

    public Coord getFarthestIdealRadarPosition(Coord actualPosition) {
        Coord farthestPosition = actualPosition;

        double bestDistance = 0;
        for (int x=28; x > 0; x=x-5) {
            for (int y=12; y >= 0; y=y-5) {
                Coord currCord = new Coord(x,y);
                if (!myTrapPos.contains(currCord) && !myRadarPos.contains(currCord)) {
                    double currentDistance = currCord.distance(actualPosition);
                    if (currentDistance>bestDistance) {
                        bestDistance = currentDistance;
                        farthestPosition = currCord;
                    }
                }
            }
        }

        return farthestPosition;
    }

    public boolean hasSafeVisibleOrePosition() {
        return !this.myVisibleOrePos.isEmpty();
    }

    public Coord getNearestVisibleOrePosition(Coord actualPosition) {
        Optional<Coord> nearestPosition = Optional.ofNullable(actualPosition);

        nearestPosition = myVisibleOrePos.stream().sorted((Object c1, Object c2) ->
                Integer.compare(actualPosition.distance(((Coord) c1)), actualPosition.distance(((Coord) c2)))).findFirst();

        return nearestPosition.get();
    }

    public Optional<Entity> whoIsMyAllyNearestFromThisCoord(Coord coord) {
        return myTeam.robots.stream().sorted((Object r1, Object r2) ->
                Integer.compare(coord.distance(((Entity) r1).pos), coord.distance(((Entity) r2).pos))).findFirst();
    }

    public Optional<Entity> whoIsMyAllyNearestFromHeadQuarter() {
        return myTeam.robots.stream().sorted((Object r1, Object r2) ->
                Integer.compare((new Coord(0, ((Entity) r1).pos.y)).distance(((Entity) r1).pos),
                        (new Coord(0, ((Entity) r2).pos.y)).distance(((Entity) r2).pos))).findFirst();
    }

    public Optional<Entity> whoIsMyAllyNearestFromThisCoordWithoutItem(Coord coord, EntityType itemType) {
        return myTeam.robots.stream().filter(r -> r.item == null || !r.item.equals(itemType)).sorted((Object r1, Object r2) ->
                Integer.compare(coord.distance(((Entity) r1).pos), coord.distance(((Entity) r2).pos))).findFirst();
    }


    public boolean isThisPosIsSafe(Coord pos) {
        Cell currentCell = this.getCell(pos);
        if (myTrapPos.contains(pos)) {
            return false;
        } else {
            return true;
        }
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

        int maxX=board.width-1,maxY= board.height-1;
        List<Coord> coords = board.getAllCoordsAccessibleFrom(currentRobot.pos);
        Action action = Action.move(pickRandomCoordFrom(coords));
        action.efficiencyRate=EfficiencyRate.WEAK;
        action.message = getMessage();
        return action;
    }

    private Coord pickRandomCoordFrom(List<Coord> coords) {
        int nbOfPossibleMoves = coords.size();
        Random rand = new Random();
        return coords.get(rand.nextInt(0, nbOfPossibleMoves-1));
    }

    @Override
    public String getMessage() {
        return ("RM");
    }
}


class FollowOreFoundWithRadarRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Coord coordToFollow = currentRobot.pos;
        final EfficiencyRate[] efficiency = {EfficiencyRate.USELESS};

        if (board.hasSafeVisibleOrePosition() && currentRobot.item.equals(EntityType.NOTHING)) {
            coordToFollow = board.getNearestVisibleOrePosition(currentRobot.pos);
            efficiency[0] = EfficiencyRate.MAXIMUM;
        }
        Action action = Action.move(coordToFollow);

        if (board.isTargetAccessibleFromActualPosition(coordToFollow, currentRobot.pos)
                && board.isThisPosIsSafe(currentRobot.pos)) {
            action = Action.dig(coordToFollow);
            action.message = "DIG 1";
        } else {
            action.message = getMessage();
        }
        action.efficiencyRate = efficiency[0];


        return action;
    }

    @Override
    public String getMessage() {
        return ("FOF 1");
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
        rules.add(new KeepPreviousActionRule());
        rules.add(new RandomMoveRule());
        rules.add(new FollowOreFoundWithoutRadarRule());
        rules.add(new FollowOreFoundWithRadarRule());
        rules.add(new RequestRadarRule());
        rules.add(new DigForRadarRule());
        rules.add(new RequestTrapRule());
        rules.add(new DigForTrapRule());
        // rules.add(new FollowOreAndDigRule());
        rules.add(new BackToHeadQuarterRule());

        // rules.add(new DigHereForPutTrapRule());


    }

    public List<Action> computeEligibleActionsRankedByEfficiency(Board board, Entity allyRobot) {
        List<Action> actionsList = new ArrayList<>();
        for (IRule rule:rules) {
            Action actionComputed = rule.evaluateAction(board, allyRobot);
            if (actionComputed.efficiencyRate.getValue()>EfficiencyRate.USELESS.getValue()) {
                actionsList.add(actionComputed);
            }
        }
        Collections.sort(actionsList);
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
        action.message = getMessage();
        return action;
    }

    @Override
    public String getMessage() {
        return ("HQ");
    }


}


class RequestRadarRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.request(EntityType.RADAR);

        if (board.myRadarCooldown==0) {
            System.err.println("near " + board.whoIsMyAllyNearestFromHeadQuarter().get().id);
        }

        if (board.myRadarCooldown==0
                && currentRobot.item.equals(EntityType.NOTHING)
                && board.whoIsMyAllyNearestFromHeadQuarter().get().id == currentRobot.id) {
            if (currentRobot.pos.x != 0) {
                action = Action.move(new Coord(0,currentRobot.pos.y));
            }
            action.efficiencyRate=EfficiencyRate.HIGH;
        } else {
            action.efficiencyRate=EfficiencyRate.USELESS;
        }

        action.message = getMessage();
        return action;
    }

    @Override
    public String getMessage() {
        return ("REQ R");
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


class DigForTrapRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.none();
        if (currentRobot.item.equals(EntityType.TRAP)) {
            Coord idealPos = board.getNearestIdealTrapPosition(currentRobot.pos);
            if (currentRobot.previousAction != null
                    && currentRobot.previousAction.message.equals(getMessage())) {
                idealPos = currentRobot.previousAction.pos;
            }

            if (!board.isTargetAccessibleFromActualPosition(idealPos, currentRobot.pos)) {
                action = Action.move(idealPos);
            } else {
                action = Action.dig(idealPos);
            }
            action.efficiencyRate = EfficiencyRate.HIGH;
        } else {
            action.efficiencyRate = EfficiencyRate.USELESS;
        }
        action.message = getMessage();
        return action;
    }

    @Override
    public String getMessage() {
        return ("DIG T");
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
        System.err.println("New message : " + message);
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


class DigForRadarRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.none();
        if (currentRobot.item.equals(EntityType.RADAR)) {
            Coord idealPos = board.getNearestIdealRadarPosition(currentRobot.pos);
            if (currentRobot.previousAction != null
                    && currentRobot.previousAction.message.equals(getMessage())) {
                idealPos = currentRobot.previousAction.pos;
            }

            if (!board.isTargetAccessibleFromActualPosition(idealPos, currentRobot.pos)) {
                action = Action.move(idealPos);
            } else {
                action = Action.dig(idealPos);
            }
            action.efficiencyRate = EfficiencyRate.HIGH;
        } else {
            action.efficiencyRate = EfficiencyRate.USELESS;
        }
        action.message = getMessage();
        return action;
    }

    @Override
    public String getMessage() {
        return ("DIG R");
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

        action.message = getMessage();
        return action;
    }

    @Override
    public String getMessage() {
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
                            board.hub.pub(new Message("ORE-FOUND", "Ore found (" + currentRobot.pos + ")", currentRobot.pos,1));
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
                            .filter(a-> !isActionAlreadyPlayed(a, actionsToPlay)).findFirst();
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

    public static Boolean isActionAlreadyPlayed(Action action, Map<Integer, Action> otherPendingActions) {
        if (action.command.startsWith("REQUEST RADAR")) {
            return otherPendingActions.values().stream().filter(a -> a.command.startsWith("REQUEST RADAR")).count() > 0;
        } else if (action.command.startsWith("REQUEST TRAP")) {
            return otherPendingActions.values().stream().filter(a -> a.command.startsWith("REQUEST TRAP")).count() > 0;
        } else if (action.message.equals("HQ")) {
            return false;
        } else {
            return otherPendingActions.containsValue(action);
        }
    }
}


class KeepPreviousActionRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = currentRobot.previousAction;
        if (action != null && action.message.equals("FOF2")) {
            return action;
        } else {
            action = Action.none();
            action.efficiencyRate=EfficiencyRate.USELESS;
            return action;
        }
    }

    @Override
    public String getMessage() {
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
                ", pos='" + pos + '\'' +
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
            Coord bestCoordToFollow = board.getFarthestIdealRadarPosition(currentRobot.pos);

            if (bestCoordToFollow.equals(new Coord(currentRobot.pos.x, currentRobot.pos.y))){
                efficiencyRate=EfficiencyRate.USELESS;
            }

            coordToFollow=bestCoordToFollow;
            System.err.println("best move to put radar : " + coordToFollow);
        }
        Action action = Action.move(coordToFollow);
        action.efficiencyRate = efficiencyRate;
        action.message = getMessage();
        return action;
    }

    @Override
    public String getMessage() {
        return "GBPR";
    }
}


interface IRule {

    Action evaluateAction(Board board, Entity currentRobot);

    String getMessage();
}
