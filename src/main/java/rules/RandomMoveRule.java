package rules;

import rules.IRule;
import support.Action;
import support.Board;
import support.Coord;
import support.Entity;

import java.util.List;
import java.util.Random;

public class RandomMoveRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {

        int maxX=board.width-1,maxY= board.height-1;
        List<Coord> coords = board.getAllCoordsAccessibleFrom(currentRobot.pos);
        Action action = Action.move(pickRandomCoordFrom(coords));
        action.efficiencyRate=EfficiencyRate.WEAK;
        action.message = getMessage();
        return action;
    }

    private Coord pickRandomCoordFrom(List<Coord> coords) {
        int nbOfPossibleMoves = coords.size();
        Random rand = new Random();
        return coords.get(rand.nextInt(0, nbOfPossibleMoves-1));
    }

    @Override
    public String getMessage() {
        return ("RM");
    }
}
