package rules;

import support.*;

import java.util.ArrayList;
import java.util.Collection;

public class SmartKamikazeRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.none();
        if (currentRobot.id == 0 && board.roundNumber<83) {
            if (currentRobot.pos.x==0 && board.myTrapCooldown==0 && currentRobot.item.equals(EntityType.NOTHING) && !board.myPossibleTrapPositions.isEmpty()) {
                action = Action.request(EntityType.TRAP);
            } else if (currentRobot.item.equals(EntityType.TRAP) && !board.myPossibleTrapPositions.isEmpty()) {
                final Coord[] bestCoordToFollow = {new Coord(0, currentRobot.pos.y)};
                final double[] bestDistance = {Integer.MAX_VALUE};

                board.myPossibleTrapPositions.stream().forEach(currCord -> {
                    double currentDistance = currCord.distance(currentRobot.pos);
                    if (currentDistance< bestDistance[0]) {
                        bestDistance[0] = currentDistance;
                        bestCoordToFollow[0] = currCord;
                    }
                });
                action = Action.dig(bestCoordToFollow[0]);
            } else if (board.myTrapCooldown==0 && !board.myPossibleTrapPositions.isEmpty()) {
                action = Action.move(new Coord(0,currentRobot.pos.y));
            } else if (board.myPossibleTrapPositions.isEmpty()){
                // mode veille
                action = Action.move(new Coord(3,currentRobot.pos.y));
            } else {
                // mode veille
                action = Action.none();
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
        return ("SK");
    }
}
