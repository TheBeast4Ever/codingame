package rules;


import support.*;

public class BackToHeadQuarterRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.move(new Coord(0, currentRobot.pos.y));
        int efficiency=0;

        if (currentRobot.item.equals(EntityType.AMADEUSIUM)) {
            efficiency=100;

        }
        action.efficiency = efficiency;
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("Hey, i've ore, go back to head quarter : (" + currentRobot.pos.x + " ; " + currentRobot.pos.y + ")");
    }


}
