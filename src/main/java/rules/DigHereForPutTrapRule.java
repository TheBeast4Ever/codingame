package rules;

import rules.IRule;
import support.*;

public class DigHereForPutTrapRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentRobot.pos.x!=0 && currentRobot.item.equals(EntityType.TRAP) && currentCell.known && !board.myTrapPos.contains(currentRobot.pos)) {
            action.efficiencyRate = EfficiencyRate.HIGH;
        } else if (currentRobot.pos.x!=0 && currentRobot.item.equals(EntityType.TRAP) && !board.myTrapPos.contains(currentRobot.pos)) {
            action.efficiencyRate = EfficiencyRate.AVERAGE;
        } else {
            action.efficiencyRate = EfficiencyRate.USELESS;
        }
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("DHFPT");
    }
}
