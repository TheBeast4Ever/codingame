package rules;

import rules.IRule;
import support.Action;
import support.Board;
import support.Entity;
import support.EntityType;

public class RequestRadarRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.request(EntityType.RADAR);
        if (currentRobot.pos.x!=0) {
            action.efficiencyRate=EfficiencyRate.USELESS;
        } else {
            if (board.myRadarCooldown==0 && currentRobot.item.equals(EntityType.NOTHING)) {
                action.efficiencyRate=EfficiencyRate.MAXIMUM;
            } else {
                action.efficiencyRate=EfficiencyRate.USELESS;
            }
        }

        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("REQ R");
    }
}
