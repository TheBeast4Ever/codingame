package rules;

import support.*;

public class FollowOreAndDigRule implements IRule {

    private boolean invisibleOreHasBeenFound(Board board) {
        Message message = board.hub.consumeWithoutRemove();
        if (message != null) {
            if (message.header.equals("ORE-FOUND")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    public Action evaluateAction(Board board, Entity currentRobot) {
        Coord coordToFollow = new Coord(0,0);
        final EfficiencyRate[] efficiency = {EfficiencyRate.USELESS};
        if (!board.myVisibleOrePos.isEmpty() && currentRobot.item.equals(EntityType.NOTHING)) {
            final Coord[] bestCoordToFollow = {new Coord(0, 0)};
            final double[] bestDistance = {Integer.MAX_VALUE};
            board.myVisibleOrePos.stream().filter(pos-> !board.myTrapPos.contains(pos)).forEach(currCord -> {
                double currentDistance = currCord.distance(currentRobot.pos);
                if (currentDistance< bestDistance[0]) {
                    bestDistance[0] = currentDistance;
                    bestCoordToFollow[0] = currCord;
                    if (currCord.equals(currentRobot.pos)) {
                        efficiency[0] = EfficiencyRate.USELESS;
                    } else {
                        efficiency[0] = EfficiencyRate.HIGH;
                    }

                }
            });
            coordToFollow=bestCoordToFollow[0];
            System.err.println("best to follow : " + coordToFollow);
        } else if (invisibleOreHasBeenFound(board) && currentRobot.item.equals(EntityType.NOTHING)
                && (currentRobot.previousAction == null || !currentRobot.previousAction.message.equals(getMessage()))) {
            System.err.println("msg received by " + currentRobot.id);
            Message message = board.hub.consumeAndRemove();
            coordToFollow = message.pos;
            efficiency[0] = EfficiencyRate.MAXIMUM;
        }
        Action action = Action.move(coordToFollow);
        if (coordToFollow.equals(currentRobot.pos)) {
            action = Action.dig(coordToFollow);
        }
        action.efficiencyRate = efficiency[0];
        action.message = getMessage();
        return action;
    }


    private Action evaluateActionOLD(Board board, Entity currentRobot) {
        Coord coordToFollow = new Coord(0,0);
        final EfficiencyRate[] efficiency = {EfficiencyRate.USELESS};
        if (!board.myVisibleOrePos.isEmpty() && currentRobot.item.equals(EntityType.NOTHING)) {
            final Coord[] bestCoordToFollow = {new Coord(0, 0)};
            final double[] bestDistance = {Integer.MAX_VALUE};
            board.myVisibleOrePos.stream().filter(pos-> !board.myTrapPos.contains(pos)).forEach(currCord -> {
                double currentDistance = currCord.distance(currentRobot.pos);
                if (currentDistance< bestDistance[0]) {
                    bestDistance[0] = currentDistance;
                    bestCoordToFollow[0] = currCord;
                    if (currCord.equals(currentRobot.pos)) {
                        efficiency[0] = EfficiencyRate.USELESS;
                    } else {
                        efficiency[0] = EfficiencyRate.HIGH;
                    }

                }
            });
            coordToFollow=bestCoordToFollow[0];
            System.err.println("best to follow : " + coordToFollow);
        } else if (invisibleOreHasBeenFound(board) && currentRobot.item.equals(EntityType.NOTHING)
        && (currentRobot.previousAction == null || !currentRobot.previousAction.command.equals(getMessage()))) {
            System.err.println("msg received by " + currentRobot.id);
            Message message = board.hub.consumeAndRemove();
            coordToFollow = message.pos;
            efficiency[0] = EfficiencyRate.MAXIMUM;
        }
        Action action = Action.move(coordToFollow);
        if (coordToFollow.equals(currentRobot.pos)) {
            action = Action.dig(coordToFollow);
        }
        action.efficiencyRate = efficiency[0];
        action.message = getMessage();
        return action;
    }

    @Override
    public String getMessage() {
        return ("FOAD");
    }
}
