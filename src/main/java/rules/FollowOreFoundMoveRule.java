package rules;

import rules.IRule;
import support.*;

public class FollowOreFoundMoveRule implements IRule {

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
        if (!board.myVisibleOrePos.isEmpty() && currentRobot.item.equals(EntityType.NOTHING)) {
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
            System.err.println("msg received by " + currentRobot.id);
            Message message = board.hub.consumeAndRemove();
            coordToFollow = message.pos;
            efficiency[0] = EfficiencyRate.MAXIMUM;
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
