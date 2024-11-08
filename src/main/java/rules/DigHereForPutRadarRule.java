package rules;

import rules.IRule;
import support.*;

public class DigHereForPutRadarRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentRobot.pos.x>0 && currentRobot.item.equals(EntityType.RADAR)  && !currentCell.hole  && !currentCell.known && !board.myTrapPos.contains(currentRobot.pos)) {
            action.efficiencyRate = EfficiencyRate.HIGH;
        } else {
            action.efficiencyRate = EfficiencyRate.USELESS;
        }
        action.message = getMessage();
        return action;
    }

    @Override
    public String getMessage() {
        return ("DHFPR");
    }
}
