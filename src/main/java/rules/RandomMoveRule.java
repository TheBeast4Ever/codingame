package rules;

import rules.IRule;
import support.Action;
import support.Board;
import support.Coord;
import support.Entity;

import java.util.Random;

public class RandomMoveRule implements IRule {

    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Random rand = new Random();
        int maxX=board.width,maxY= board.height;
        Action action = Action.move(new Coord(rand.nextInt(maxX+1)-1, rand.nextInt(maxY+1)-1));
        action.efficiency=10;
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("My brain is off (#" + currentRobot.id + ")");
    }
}
