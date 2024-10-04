public class BackToHeadQuarterRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action=Action.move(new Coord(0, currentRobot.pos.y));

        if (currentRobot.item.equals(EntityType.AMADEUSIUM)) {
            action.setEfficiency(100);
            System.err.println("Hey, i've ore, go back to head quarter : (" + currentRobot.pos.x + " ; " + currentRobot.pos.y + ")");
        } else {
            action.setEfficiency(0);
        }
        return action;
    }
}
