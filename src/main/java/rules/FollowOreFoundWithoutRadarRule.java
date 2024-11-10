package rules;

import support.*;

public class FollowOreFoundWithoutRadarRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Coord coordToFollow = new Coord(0,0);
        final EfficiencyRate[] efficiency = {EfficiencyRate.USELESS};

        if (messageOreFoundPublished(board)) {
            System.err.println("msg " + board.hub.consumeWithoutRemove().id);
            System.err.println("near " + board.whoIsMyAllyNearestFromThisCoord(board.hub.consumeWithoutRemove().pos).get().id);
        }

        if (messageOreFoundPublished(board)
                && currentRobot.item.equals(EntityType.NOTHING)
                && board.whoIsMyAllyNearestFromThisCoord(board.hub.consumeWithoutRemove().pos).get().equals(currentRobot)) {
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
        /*System.err.println("id=" + currentRobot.id);
        System.err.println("coordToFollow=" + coordToFollow);
        System.err.println("currentRobot.pos=" + currentRobot.pos);*/
        if (coordToFollow.equals(currentRobot.pos)) {
            action = Action.dig(coordToFollow);
            action.message = "DIG";
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
