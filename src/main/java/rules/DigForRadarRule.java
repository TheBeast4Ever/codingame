package rules;

import support.*;

public class DigForRadarRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.none();
        if (currentRobot.item.equals(EntityType.RADAR)) {
            Coord idealPos = board.getNearestIdealRadarPosition(currentRobot.pos);
            if (currentRobot.previousAction != null
                    && currentRobot.previousAction.message.equals(getMessage())) {
                idealPos = currentRobot.previousAction.pos;
            }

            action = Action.dig(idealPos);
            if (board.isThisPosIsSafe(idealPos)) {
                action.efficiencyRate = EfficiencyRate.HIGH;
            } else {
                action.efficiencyRate = EfficiencyRate.USELESS;
            }
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
