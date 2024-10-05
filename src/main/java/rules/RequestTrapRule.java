package rules;

import rules.IRule;
import support.Action;
import support.Board;
import support.Entity;
import support.EntityType;

public class RequestTrapRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.request(EntityType.TRAP);
        if (currentRobot.pos.x!=0) {
            action.efficiency=0;
        } else {
            if (board.myTrapCooldown==0 && currentRobot.item.equals(EntityType.NOTHING)) {
                action.efficiency=60;
            } else {
                action.efficiency=0;
            }
        }

        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("It's time to get a trap (#" + currentRobot.id + ")");
    }
}
