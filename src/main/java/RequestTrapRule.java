public class RequestTrapRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.request(EntityType.TRAP);
        if (currentRobot.pos.x!=0) {
            action.setEfficiency(0);
        } else {
            if (board.myTrapCooldown==0 && currentRobot.item.equals(EntityType.NOTHING)) {
                action.setEfficiency(60);
            } else {
                action.setEfficiency(0);
            }
        }

        return action;
    }
}
