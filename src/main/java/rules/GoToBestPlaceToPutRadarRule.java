package rules;

import support.*;

public class GoToBestPlaceToPutRadarRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Coord coordToFollow = new Coord(0,0);
        int efficiency = 0;
        if (currentRobot.item.equals(EntityType.RADAR)) {
            efficiency=70;
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
                efficiency=0;
            }

            coordToFollow=bestCoordToFollow;
            System.err.println("best move to put radar : " + coordToFollow);
        }
        Action action = Action.move(coordToFollow);
        action.efficiency = efficiency;
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return "GBPR";
    }
}
