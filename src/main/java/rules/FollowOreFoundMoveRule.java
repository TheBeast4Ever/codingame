package rules;

import rules.IRule;
import support.*;

public class FollowOreFoundMoveRule implements IRule {
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
