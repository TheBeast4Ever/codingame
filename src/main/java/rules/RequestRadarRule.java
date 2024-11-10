package rules;

import rules.IRule;
import support.*;

public class RequestRadarRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.request(EntityType.RADAR);

        if (board.myRadarCooldown==0
                && currentRobot.item.equals(EntityType.NOTHING)
                && board.whoIsMyAllyNearestFromThisCoord(new Coord(0,currentRobot.pos.y)).get().equals(currentRobot)) {
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
        return ("REQ R");
    }
}
