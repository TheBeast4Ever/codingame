package rules;

import support.Action;
import support.Board;
import support.Coord;
import support.Entity;

import java.util.Random;

public class RandomMoveFor50FirstRoundsRule implements IRule {
    @Override
    public Action evaluateAction(Board board, Entity currentRobot) {
        Random rand = new Random();
        int offset=15;
        int maxX=board.width-offset,maxY= board.height;
        Action action = Action.move(new Coord(offset+rand.nextInt(maxX+1)-1, rand.nextInt(maxY+1)-1));
        int efficiency=0;
        if (board.roundNumber<=50) {
            efficiency=20;
        }
        action.efficiency = efficiency;
        action.message = getMessage(currentRobot);
        return action;
    }

    @Override
    public String getMessage(Entity currentRobot) {
        return ("My brain is off for the first 50 rounds (#" + currentRobot.id + ")");
    }
}
