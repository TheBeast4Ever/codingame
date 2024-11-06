package rules;


import support.*;

public class BackToHeadQuarterRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.move(new Coord(0, currentRobot.pos.y));
        EfficiencyRate efficiencyRateToCompute = EfficiencyRate.USELESS;

        if (currentRobot.item.equals(EntityType.AMADEUSIUM)) {
            efficiencyRateToCompute= EfficiencyRate.MAXIMUM;

        }
        action.efficiencyRate = efficiencyRateToCompute;
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("HQ");
    }


}
