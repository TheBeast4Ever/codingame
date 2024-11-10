package rules;

import support.*;

public class FollowOreFoundWithRadarRule implements IRule {

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
