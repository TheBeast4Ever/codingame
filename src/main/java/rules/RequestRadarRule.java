package rules;

import rules.IRule;
import support.*;

public class RequestRadarRule implements IRule {
    public static final int MAX_VISIBLE_ORE_TO_REQUEST_RADAR = 10;

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.request(EntityType.RADAR);

        if (board.myRadarCooldown==0
                && board.nbOfSafeVisibleOrePosition()<=MAX_VISIBLE_ORE_TO_REQUEST_RADAR
                && currentRobot.item.equals(EntityType.NOTHING)
                && (currentRobot.previousAction != null
                    && (currentRobot.previousAction.message.equals(getMessage()) || currentRobot.previousAction.efficiencyRate.getValue()<EfficiencyRate.HIGH.getValue()))
                && board.whoIsMyAllyNearestFromHeadQuarter().isPresent()
                && board.whoIsMyAllyNearestFromHeadQuarter().get().id == currentRobot.id) {
            if (currentRobot.pos.x != 0) {
                action = Action.move(new Coord(0,currentRobot.pos.y));
            }
            action.efficiencyRate=EfficiencyRate.MAXIMUM;
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
