package rules;

import support.*;

public class FollowOreFoundWithoutRadarRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action;
        Coord coordToFollow = new Coord(0,0);
         EfficiencyRate efficiency = EfficiencyRate.USELESS;

        if (messageOreFoundPublished(board)
                && currentRobot.item.equals(EntityType.NOTHING)
                && board.isThisPosIsSafe(board.hub.consumeWithoutRemove().pos)
                && board.whoIsMyAllyNearestFromThisCoordWithoutItem(board.hub.consumeWithoutRemove().pos, EntityType.AMADEUSIUM).get().equals(currentRobot)) {
            System.err.println("msg consumed by " + currentRobot.id);
            Message message = board.hub.consumeAndRemove();
            coordToFollow = message.pos;
            efficiency = EfficiencyRate.MAXIMUM;
        } else if (currentRobot.previousAction != null
                && currentRobot.previousAction.message.equals(getMessage())
                && currentRobot.item.equals(EntityType.NOTHING)
                && !board.myEmptyVisitedHoles.contains(currentRobot.previousAction.pos)) {
            coordToFollow = currentRobot.previousAction.pos;
            efficiency = EfficiencyRate.MAXIMUM;
        }

        if (coordToFollow.distance(currentRobot.pos) <= 1) {
            action = Action.dig(coordToFollow);
        } else {
            action = Action.move(coordToFollow);
        }

        action.message = getMessage();

        action.efficiencyRate = efficiency;


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
