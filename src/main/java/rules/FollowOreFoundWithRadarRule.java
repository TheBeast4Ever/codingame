package rules;

import support.*;

public class FollowOreFoundWithRadarRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action;
        Coord coordToFollow = currentRobot.pos;
        EfficiencyRate efficiency = EfficiencyRate.USELESS;

        System.err.println(currentRobot.id + "-" + board.hasSafeVisibleOrePosition());

        if (board.hasSafeVisibleOrePosition()
                && currentRobot.item.equals(EntityType.NOTHING)) {
            coordToFollow = board.getSafeNearestVisibleOrePosition(currentRobot.pos);
            System.err.println(currentRobot.id + "-FOF1(: " + coordToFollow + ")-" + board.getCell(coordToFollow).hole);

            efficiency= EfficiencyRate.HIGH;
        } else if (currentRobot.previousAction != null
                && currentRobot.previousAction.message.equals(getMessage())
                && currentRobot.item.equals(EntityType.NOTHING)
                && !board.myEmptyVisitedHoles.contains(currentRobot.previousAction.pos)
                && board.getCell(currentRobot.previousAction.pos).ore>0) {
            coordToFollow = currentRobot.previousAction.pos;
            efficiency = EfficiencyRate.HIGH;
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
        return ("FOF 1");
    }
}
