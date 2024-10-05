package rules;

import support.*;

public class DigHereForOreRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentCell.ore>0 && currentRobot.item.equals(EntityType.NOTHING) && !board.myTrapPos.contains(currentRobot.pos)) {
            action.efficiency = 99;
        } else {
            if (currentRobot.pos.x!=0 && !currentCell.hole && !board.myTrapPos.contains(currentRobot.pos)) {
                action.efficiency = 50;
            } else {
                action.efficiency = 0;
            }
        }
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("Hum, it seems good to search ore here : (" + currentRobot.pos.x + " ; " + currentRobot.pos.y + ")");
    }
}
