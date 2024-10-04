public class DigHereForOreRule implements IRule{
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.dig(currentRobot.pos);
        Cell currentCell = board.getCell(currentRobot.pos);
        if (currentCell.ore>0 && currentRobot.item.equals(EntityType.NOTHING)) {
            action.setEfficiency(99);
            System.err.println("Hum, it seems good to search ore here : (" + currentRobot.pos.x + " ; " + currentRobot.pos.y + "). Ore value is : " + currentCell.ore);
        } else {
            if (currentRobot.pos.x!=0 && !currentCell.hole) {
                action.setEfficiency(50);
            } else {
                action.setEfficiency(0);
            }
        }
        return action;
    }
}
