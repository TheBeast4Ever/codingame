package rules;

import support.*;

public class GoToBestPlaceToPutRadarRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Coord coordToFollow = new Coord(0,0);
        EfficiencyRate efficiencyRate = EfficiencyRate.USELESS;
        if (currentRobot.item.equals(EntityType.RADAR)) {
            efficiencyRate=EfficiencyRate.HIGH;
            Coord bestCoordToFollow = board.getNearestIdealRadarPosition(currentRobot.pos);

            if (bestCoordToFollow.equals(new Coord(currentRobot.pos.x, currentRobot.pos.y))){
                efficiencyRate=EfficiencyRate.USELESS;
            }

            coordToFollow=bestCoordToFollow;
            System.err.println("best move to put radar : " + coordToFollow);
        }
        Action action = Action.move(coordToFollow);
        action.efficiencyRate = efficiencyRate;
        action.message = getMessage();
        return action;
    }

    @Override
    public String getMessage() {
        return "GBPR";
    }
}
