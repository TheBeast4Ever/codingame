package support;

import java.util.*;

public class Board {
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

    public Coord getNearestIdealRadarPosition(Coord actualPosition) {
        Coord nearestPosition = actualPosition;

        double bestDistance = Integer.MAX_VALUE;
        for (int x=28; x > 0; x=x-5) {
            for (int y=12; y >= 0; y=y-5) {
                Coord currCord = new Coord(x,y);
                if (!myTrapPos.contains(currCord) && !myRadarPos.contains(currCord)) {
                    double currentDistance = currCord.distance(actualPosition);
                    /*if (currentDistance<bestDistance) {
                        bestDistance = currentDistance;
                        nearestPosition = currCord;
                    }*/
                    nearestPosition = currCord;
                    break;
                }
            }
        }

        return nearestPosition;
    }
}
