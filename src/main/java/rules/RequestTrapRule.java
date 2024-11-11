package rules;

import rules.IRule;
import support.*;

public class RequestTrapRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.request(EntityType.TRAP);

        if (board.myTrapCooldown==0
                && currentRobot.item.equals(EntityType.NOTHING)
                && board.whoIsMyAllyNearestFromHeadQuarter().isPresent()
                && board.whoIsMyAllyNearestFromHeadQuarter().get().id == currentRobot.id) {
            if (currentRobot.pos.x != 0) {
                action = Action.move(new Coord(0,currentRobot.pos.y));
            }
            action.efficiencyRate=EfficiencyRate.HIGH;
        } else {
            action.efficiencyRate=EfficiencyRate.USELESS;
        }

        action.message = getMessage();
        return action;
    }

    @Override
    public String getMessage() {
        return ("REQ T");
    }
}
