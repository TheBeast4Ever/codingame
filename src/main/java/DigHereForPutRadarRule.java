public class DigHereForPutRadarRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentRobot.pos.x!=0 && currentRobot.item.equals(EntityType.RADAR) && !currentCell.known) {
            action.setEfficiency(90);
            System.err.println("Hum, it seems good to put a radar here : (" + currentRobot.pos.x + " ; " + currentRobot.pos.y + ")");
        } else {
            action.setEfficiency(0);
        }
        return action;
    }
}
