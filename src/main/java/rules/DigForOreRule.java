package rules;

import support.*;

public class DigForOreRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentRobot.item.equals(EntityType.NOTHING)
                && currentRobot.pos.x != 0
                && !board.myTrapPos.contains(currentRobot.pos)
                && !board.myUnsuccessfulHoles.contains(currentRobot.pos)
                && !currentCell.hole) {
            if (currentCell.ore > 0) {
                action.efficiencyRate = EfficiencyRate.MAXIMUM;
            } else {
                if (!board.myTrapPos.contains(currentRobot.pos)) {
                    action.efficiencyRate = EfficiencyRate.AVERAGE;
                } else {
                    if (!board.myTrapPos.contains(currentRobot.pos)) {
                        action.efficiencyRate = EfficiencyRate.WEAK;
                    } else {
                        action.efficiencyRate = EfficiencyRate.USELESS;
                    }
                }
            }
        } else {
            action.efficiencyRate = EfficiencyRate.USELESS;
        }
        action.message = getMessage();
        return action;
    }

    @Override
    public String getMessage() {
        return ("DIG");
    }
}
