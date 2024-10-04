public class FollowOreFoundMoveRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Coord coordToFollow = new Coord(0,0);
        final int[] efficiency = {0};
        if (!board.myVisibleOrePos.isEmpty() && currentRobot.item.equals(EntityType.NOTHING)) {
            final Coord[] bestCoordToFollow = {new Coord(0, 0)};
            final double[] bestDistance = {Integer.MAX_VALUE};
            board.myVisibleOrePos.stream().forEach(currCord -> {
                double currentDistance = currCord.distance(currentRobot.pos);
                if (currentDistance< bestDistance[0]) {
                    bestDistance[0] = currentDistance;
                    bestCoordToFollow[0] = currCord;
                }
            });
            coordToFollow=bestCoordToFollow[0];
            efficiency[0] =90;
            System.err.println("Hey, it smells ore at : (" + coordToFollow + ")");
        }
        Action action = Action.move(coordToFollow);
        action.setEfficiency(efficiency[0]);
        return action;
    }
}
