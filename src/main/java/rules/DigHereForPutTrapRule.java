package rules;

import rules.IRule;
import support.*;

public class DigHereForPutTrapRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentRobot.pos.x!=0 && currentRobot.item.equals(EntityType.TRAP) && currentCell.known && currentCell.ore==0  && !board.myTrapPos.contains(currentRobot.pos)) {
            action.efficiency = 90;
        } else {
            action.efficiency = 0;
        }
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("Hum, it seems good to put a trap here : (" + currentRobot.pos.x + " ; " + currentRobot.pos.y + ")");
    }
}
