package rules;

import support.Action;
import support.Board;
import support.Entity;

public class KeepPreviousActionRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = currentRobot.previousAction;
        if (action != null && action.message.equals("FOF2")) {
            return action;
        } else {
            action = Action.none();
            action.efficiencyRate=EfficiencyRate.USELESS;
            return action;
        }
    }

    @Override
    public String getMessage() {
        return ("KPA");
    }
}
