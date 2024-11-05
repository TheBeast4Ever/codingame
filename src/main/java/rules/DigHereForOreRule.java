package rules;

import support.*;

public class DigHereForOreRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (!currentRobot.item.equals(EntityType.AMADEUSIUM)) {
            if (currentCell.ore > 0 && currentRobot.item.equals(EntityType.NOTHING) && !board.myTrapPos.contains(currentRobot.pos)) {
                action.efficiency = 100;
            } else {
                if (currentRobot.pos.x != 0 && !currentCell.hole && !board.myTrapPos.contains(currentRobot.pos)) {
                    action.efficiency = 50;
                } else {
                    action.efficiency = 0;
                }
            }
        } else {
            action.efficiency = 0;
        }
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("DHFO");
    }
}
