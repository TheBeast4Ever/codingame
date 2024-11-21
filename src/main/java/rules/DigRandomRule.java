package rules;

import support.*;

import java.util.List;
import java.util.Random;

public class DigRandomRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Action action = Action.none();
        if (currentRobot.previousAction != null
                && currentRobot.previousAction.message.equals(getMessage())
                && currentRobot.previousAction.command.startsWith("MOVE")
                && currentRobot.item.equals(EntityType.NOTHING)
                && !board.myEmptyVisitedHoles.contains(currentRobot.previousAction.pos)) {
            action = Action.dig(currentRobot.previousAction.pos);
            action.efficiencyRate = EfficiencyRate.WEAK;
        } else {
            List<Coord> coords = board.getAllCoordsAccessibleAndNotVisitedFrom(currentRobot.pos, 4);
            if (!coords.isEmpty()) {
                Coord targetPos = pickRandomCoordFrom(coords);
                if (targetPos.distance(currentRobot.pos) <= 1) {
                    action = Action.dig(targetPos);
                } else {
                    action = Action.move(targetPos);
                }
                System.err.println(currentRobot.id + "-RM1(: " + action.pos + ")-" + board.getCell(action.pos).hole);
                action.efficiencyRate=EfficiencyRate.WEAK;
            } else {
                coords = board.getAllCoordsAccessibleAndNotVisitedFrom(currentRobot.pos, 8);
                if (!coords.isEmpty()) {
                    Coord targetPos = pickRandomCoordFrom(coords);
                    if (targetPos.distance(currentRobot.pos) == 1) {
                        action = Action.dig(targetPos);
                    } else {
                        action = Action.move(targetPos);
                    }
                    System.err.println(currentRobot.id + "-RM2(: " + action.pos + ")-" + board.getCell(action.pos).hole);
                    action.efficiencyRate = EfficiencyRate.WEAK;
                } else {
                    System.err.println("No coord accessible");
                    action.efficiencyRate = EfficiencyRate.USELESS;
                }
            }
        }

        action.message = getMessage();
        return action;
    }

    private Coord pickRandomCoordFrom(List<Coord> coords) {
        int nbOfPossibleMoves = coords.size();
        Random rand = new Random();
        if (nbOfPossibleMoves>1) {
            return coords.get(rand.nextInt(0, nbOfPossibleMoves-1));
        } else {
            return coords.get(0);
        }
    }

    @Override
    public String getMessage() {
        return ("RM");
    }
}
