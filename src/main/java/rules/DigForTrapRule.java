package rules;

import support.*;

public class DigForTrapRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.none();
        if (currentRobot.item.equals(EntityType.TRAP)) {
            Coord idealPos = board.getNearestObviousOpponentRadarPosition(currentRobot.pos);
            if (idealPos == null) {
                idealPos = board.getNearestIdealTrapPosition(currentRobot.pos);
            }

            if (currentRobot.previousAction != null
                    && currentRobot.previousAction.message.equals(getMessage())) {
                idealPos = currentRobot.previousAction.pos;
            }
            if (idealPos.distance(currentRobot.pos) <= 1) {
                action = Action.dig(idealPos);
            } else {
                action = Action.move(idealPos);
            }

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
        return ("DIG T");
    }
}
