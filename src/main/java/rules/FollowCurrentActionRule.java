package rules;

import support.Action;
import support.Board;
import support.Entity;

public class FollowCurrentActionRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = currentRobot.action;
        if (action != null) {
            return action;
        } else {
            action = Action.none();
            action.efficiency=0;
            return action;
        }
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("FCA");
    }
}
