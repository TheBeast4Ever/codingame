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

    public Collection<Coord> myObviousOpponentRadarPos = new ArrayList<Coord>();

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

    private void initAndUpdateMyObviousOpponentRadarPos() {
        for (int x=1; x < width-1; x=x+7) {
            for (int y=0; y < height-1; y=y+7) {
                Coord currCord = new Coord(x,y);
                if (!this.getCell(currCord).hole) {
                    if (!myObviousOpponentRadarPos.contains(currCord)) {
                        myObviousOpponentRadarPos.add(currCord);
                    }
                } else {
                    if (myObviousOpponentRadarPos.contains(currCord)) {
                        myObviousOpponentRadarPos.remove(currCord);
                    }
                }
            }
        }
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
        initAndUpdateMyObviousOpponentRadarPos();
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
        if (nearestPosition.x==0) {
            nearestPosition = new Coord(nearestPosition.x+1, nearestPosition.y);
        }

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

    public Coord getNearestObviousOpponentRadarPosition(Coord actualPosition) {
        Coord nearestPosition = null;

        double bestDistance = Integer.MAX_VALUE;
        for (int x=1; x < width-1; x=x+7) {
            for (int y=0; y < height-1; y=y+7) {
                Coord currCord = new Coord(x,y);
                if (myObviousOpponentRadarPos.contains(currCord)) {
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

        if (nearestPosition.x==0) {
            nearestPosition = new Coord(nearestPosition.x+1, nearestPosition.y);
        }

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
        return myTeam.robots.stream().filter(r->r.isAlive()).sorted((Object r1, Object r2) ->
                Integer.compare(coord.distance(((Entity) r1).pos), coord.distance(((Entity) r2).pos))).findFirst();
    }

    public Optional<Entity> whoIsMyAllyNearestFromHeadQuarter() {
        return myTeam.robots.stream()
                .filter(r->r.isAlive() && (r.item.equals(EntityType.NOTHING) || r.item.equals(EntityType.AMADEUSIUM)))
                .sorted((Object r1, Object r2) ->
                Integer.compare((new Coord(0, ((Entity) r1).pos.y)).distance(((Entity) r1).pos),
                        (new Coord(0, ((Entity) r2).pos.y)).distance(((Entity) r2).pos))).findFirst();
    }

    public Optional<Entity> whoIsMyAllyNearestFromThisCoordWithoutItem(Coord coord, EntityType itemType) {
        return myTeam.robots.stream().filter(r -> r.isAlive() && (r.item == null || !r.item.equals(itemType))).sorted((Object r1, Object r2) ->
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
